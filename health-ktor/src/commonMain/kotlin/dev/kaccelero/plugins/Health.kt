package dev.kaccelero.plugins

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.util.*
import kotlinx.coroutines.*

class Health private constructor(
    val config: HealthConfiguration,
) {

    private val cache = if (config.cachingResults) HealthCheckCache() else null
    private val refreshJobs = mutableListOf<Job>()

    fun addInterceptor(pipeline: ApplicationCallPipeline) {
        val checks = config.getChecksWithFunctions().takeIf { it.isNotEmpty() } ?: return
        if (config.cachingResults) startBackgroundRefresh(pipeline, checks)
        pipeline.intercept(ApplicationCallPipeline.Call) {
            val url = call.request.path().trim('/')
            val checkFunction = checks[url] ?: return@intercept

            if (config.cachingResults && cache != null) {
                // Use cached results
                val cached = cache.get(url) ?: run {
                    // Cache not initialized yet, return ServiceUnavailable
                    call.respond(HttpStatusCode.ServiceUnavailable, emptyMap<String, Boolean>())
                    finish()
                    return@intercept
                }

                // Check if cache is stale
                val stalenessThreshold = config.cachingStalenessThreshold
                    ?: (config.cachingRefreshInterval * 3)
                if (cache.isStale(url, stalenessThreshold)) {
                    // Cache is stale, return ServiceUnavailable
                    call.respond(HttpStatusCode.ServiceUnavailable, cached.results)
                    finish()
                    return@intercept
                }

                // Return cached results
                val success = cached.results.values.all { it }
                call.respond(
                    if (success) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable,
                    cached.results
                )
                finish()
            } else {
                // No caching, execute checks synchronously (original behavior)
                val check = checkFunction.invoke()
                val success = check.values.all { it }
                call.respond(
                    if (success) HttpStatusCode.OK else HttpStatusCode.ServiceUnavailable,
                    check
                )
                finish()
            }
        }
    }

    private fun startBackgroundRefresh(
        pipeline: ApplicationCallPipeline,
        checks: Map<String, suspend () -> Map<String, Boolean>>,
    ) {
        val cache = this.cache ?: return

        // Get or create a scope for background jobs
        // If pipeline is an Application, it implements CoroutineScope
        val scope = if (pipeline is Application) {
            // Application implements CoroutineScope, use its scope with SupervisorJob
            CoroutineScope(pipeline.coroutineContext + SupervisorJob())
        } else {
            // Fallback: create independent scope
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
        }

        checks.forEach { (url, checkFunction) ->
            val checkNames = config.checks[url]?.keys ?: emptySet()
            val job = scope.launch {
                runCheck(url, checkFunction, checkNames, cache)
                while (isActive) {
                    delay(config.cachingRefreshInterval)
                    runCheck(url, checkFunction, checkNames, cache)
                }
            }
            refreshJobs.add(job)
        }
    }

    private suspend fun runCheck(
        url: String,
        checkFunction: suspend () -> Map<String, Boolean>,
        checkNames: Set<String>,
        cache: HealthCheckCache,
    ) {
        try {
            val result = withTimeout(config.cachingCheckTimeout) {
                checkFunction.invoke()
            }
            cache.update(url, result)
        } catch (_: TimeoutCancellationException) {
            // Check timed out, mark all checks as failed
            val failedResults = checkNames.associateWith { false }
            cache.update(url, failedResults)
        } catch (_: Exception) {
            // Check failed with exception, mark all checks as failed
            val failedResults = checkNames.associateWith { false }
            cache.update(url, failedResults)
        }
    }

    fun cleanup() {
        refreshJobs.forEach { it.cancel() }
        refreshJobs.clear()
    }

    companion object Feature : BaseApplicationPlugin<ApplicationCallPipeline, HealthConfiguration, Health> {

        override val key = AttributeKey<Health>("KtorHealth")
        override fun install(pipeline: ApplicationCallPipeline, configure: HealthConfiguration.() -> Unit) =
            Health(
                HealthConfiguration()
                    .apply(configure)
                    .apply { ensureWellKnown() }
            ).apply {
                addInterceptor(pipeline)
                if (pipeline is Application) pipeline.monitor.subscribe(ApplicationStopped) {
                    cleanup()
                }
            }

    }

}
