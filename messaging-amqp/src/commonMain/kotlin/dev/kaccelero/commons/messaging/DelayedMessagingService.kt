package dev.kaccelero.commons.messaging

import dev.kaccelero.serializers.Serialization
import dev.kourier.amqp.Field
import dev.kourier.amqp.properties
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

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
    maxXDeathCount,
    connectionName,
) {

    override suspend fun exchangeDeclare(exchange: IMessagingExchange, type: String, arguments: Map<String, Field>) {
        channel?.exchangeDeclare(
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
        tryWithAttempts(attempts, delay) {
            val delay = publishAt?.minus(Clock.System.now())?.inWholeMilliseconds ?: 0
            channel!!.basicPublish(
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
