package dev.kaccelero.commons.messaging

import dev.kaccelero.serializers.Serialization
import dev.kourier.amqp.Field
import dev.kourier.amqp.properties
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

/**
 * A messaging service that supports delayed message delivery using the x-delayed-message exchange type.
 */
open class DelayedMessagingService(
    host: String,
    user: String,
    password: String,
    exchange: IMessagingExchange,
    queue: IMessagingQueue,
    keys: List<IMessagingKey>,
    handleMessagingUseCaseFactory: () -> IHandleMessagingUseCase,
    coroutineScope: CoroutineScope,
    json: Json? = null,
    autoConnect: Boolean = true,
    autoListen: Boolean = true,
    persistent: Boolean = false,
    quorum: Boolean = false,
    dead: Boolean = false,
    prefetchCount: UShort = 1u,
    maxXDeathCount: Int = 1,
    connectionName: String = queue.queue,
) : MessagingService(
    host,
    user,
    password,
    exchange,
    queue,
    keys,
    handleMessagingUseCaseFactory,
    coroutineScope,
    json,
    autoConnect,
    autoListen,
    persistent,
    quorum,
    dead,
    prefetchCount,
    maxXDeathCount,
    connectionName,
) {

    override suspend fun exchangeDeclare(exchange: IMessagingExchange, type: String, arguments: Map<String, Field>) {
        withTimeoutOrNull(60.seconds) { channelReady.await() }
        val channel = this.channel ?: error("Channel is not initialized")
        channel.exchangeDeclare(
            name = exchange.exchange,
            type = "x-delayed-message",
            durable = true,
            arguments = arguments + mapOf("x-delayed-type" to Field.LongString(type))
        )
        exchangeDeclareAdditionalResources(exchange, type, arguments)
    }

    suspend inline fun <reified T> publish(
        routingKey: IMessagingKey,
        value: T,
        publishAt: Instant?,
        exchange: IMessagingExchange? = null,
        persistent: Boolean = false,
        attempts: Int = 3,
        delay: Long = 5000,
    ) {
        withTimeoutOrNull(60.seconds) { setupCompleted.await() }
        tryWithAttempts(attempts, delay) {
            val channel = this.channel ?: error("Channel is not initialized")
            val delay = publishAt?.minus(Clock.System.now())?.inWholeMilliseconds ?: 0
            channel.basicPublish(
                body = (json ?: Serialization.json).encodeToString(value).toByteArray(),
                exchange = (exchange ?: this.exchange).exchange,
                routingKey = routingKey.key,
                properties = properties {
                    deliveryMode = if (persistent || this@DelayedMessagingService.persistent) 2u else 1u
                    headers = mapOf("x-delay" to Field.Long(delay))
                },
            )
        }
    }

}
