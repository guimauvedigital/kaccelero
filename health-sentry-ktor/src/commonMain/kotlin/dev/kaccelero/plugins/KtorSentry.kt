package dev.kaccelero.plugins

import io.ktor.server.application.*
import io.ktor.server.application.hooks.*
import io.ktor.server.request.*
import io.ktor.server.routing.RoutingRoot.Plugin.RoutingCallStarted
import io.ktor.util.*
import io.sentry.*
import io.sentry.protocol.Request
import io.sentry.protocol.Response

val sentryTransactionKey = AttributeKey<ITransaction>("SentryTransaction")

class KtorSentry private constructor() {

    companion object Feature : BaseApplicationPlugin<ApplicationCallPipeline, SentryOptions, KtorSentry> {

        override val key = AttributeKey<KtorSentry>("KtorSentry")
        override fun install(pipeline: ApplicationCallPipeline, configure: SentryOptions.() -> Unit) =
            KtorSentry().apply {
                // Init Sentry
                Sentry.init {
                    it.apply(configure)

                    // Disable traces for health checks
                    if (pipeline.pluginRegistry.contains(Health.key)) {
                        val healthChecks = pipeline.plugin(Health).config.checks
                        val oldTracesSampler = it.tracesSampler
                        it.setTracesSampler { context ->
                            context.customSamplingContext?.let { samplingContext ->
                                if (context.transactionContext.parentSampled == true)
                                    return@setTracesSampler 1.0
                                if (healthChecks.containsKey((samplingContext["path"] as? String)?.trim('/')))
                                    return@setTracesSampler 0.0
                                oldTracesSampler?.sample(context) // Fallback to old sampler
                            }
                        }
                    }
                }

                // Add hooks
                MonitoringEvent(RoutingCallStarted).install(pipeline) { call ->
                    val transaction = Sentry.startTransaction(
                        /* name = */ "${call.request.httpMethod.value} ${call.request.path()}",
                        /* operation = */ "call",
                        /* transactionOptions = */TransactionOptions().apply {
                            customSamplingContext = CustomSamplingContext().apply {
                                this["path"] = call.request.path().lowercase()
                            }
                            isBindToScope = true
                        }
                    )
                    Sentry.configureScope { scope ->
                        scope.addBreadcrumb(Breadcrumb.http(call.request.uri, call.request.httpMethod.value))
                        scope.request = Request().apply {
                            method = call.request.httpMethod.value
                            url = call.request.path()
                            queryString = call.request.queryString()
                            headers = call.request.headers.toMap()
                                .mapValues { (_, v) -> v.firstOrNull() }
                        }
                        scope.setTag("url", call.request.uri)
                        scope.setTag("host", call.request.host())
                    }
                    call.attributes.put(sentryTransactionKey, transaction)
                    transaction.startChild("setup", "Call setup")
                }

                MonitoringEvent(RoutingCallStarted).install(pipeline) { call ->
                    call.sentryTransactionOrNull()?.let { t ->
                        t.latestActiveSpan?.finish(SpanStatus.OK)
                        t.setTag("route", call.route.parent.toString())
                        t.startChild("processing", "Request processing")
                    }
                }

                ResponseBodyReadyForSend.install(pipeline) { call, _ ->
                    call.sentryTransactionOrNull()?.let { t ->
                        t.latestActiveSpan?.finish(SpanStatus.OK)
                        t.startChild("sending", "Sending response")
                    }
                }

                CallFailed.install(pipeline) { call, cause ->
                    Sentry.captureException(cause)
                    call.sentryTransactionOrNull()?.apply {
                        throwable = cause
                    }
                }

                ResponseSent.install(pipeline) { call ->
                    call.sentryTransactionOrNull()?.let { t ->
                        t.latestActiveSpan?.finish(SpanStatus.OK)
                        Sentry.addBreadcrumb(
                            Breadcrumb.http(
                                call.request.uri,
                                call.request.httpMethod.value,
                                call.response.status()?.value
                            )
                        )
                        t.contexts.setResponse(
                            Response().apply {
                                headers =
                                    call.response.headers.allValues().toMap().mapValues { (_, v) -> v.firstOrNull() }
                                statusCode = call.response.status()?.value
                            },
                        )
                        t.finish(SpanStatus.fromHttpStatusCode(call.response.status()?.value, SpanStatus.OK))
                    }
                }

                SentryContextHook.install(pipeline) { block ->
                    block()
                }
            }

    }

}

fun ApplicationCall.sentryTransactionOrNull() =
    if (attributes.contains(sentryTransactionKey)) attributes[sentryTransactionKey] else null
