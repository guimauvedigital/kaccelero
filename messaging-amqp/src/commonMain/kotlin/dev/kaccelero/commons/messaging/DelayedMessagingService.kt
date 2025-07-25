package dev.kaccelero.commons.messaging

import dev.kaccelero.serializers.Serialization
import dev.kourier.amqp.Field
import dev.kourier.amqp.Properties
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

open class DelayedMessagingService(
    exchange: String,
    host: String,
    user: String,
    password: String,
    handleMessagingUseCase: IHandleMessagingUseCase,
    keys: List<IMessagingKey>,
    json: Json? = null,
    listen: Boolean = true,
    persistent: Boolean = false,
    quorum: Boolean = false,
    dead: Boolean = false,
    maxXDeathCount: Int = 1,
    connectionName: String = exchange,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : MessagingService(
    exchange,
    host,
    user,
    password,
    handleMessagingUseCase,
    keys,
    json,
    listen,
    persistent,
    quorum,
    dead,
    maxXDeathCount,
    connectionName,
    coroutineScope
) {

    override suspend fun exchangeDeclare(name: String, type: String, arguments: Map<String, Field>) {
        channel?.exchangeDeclare(
            name = name,
            type = "x-delayed-message",
            durable = true,
            autoDelete = false,
            arguments = arguments + mapOf("x-delayed-type" to Field.LongString(type))
        )
        exchangeDeclareAdditionalResources(name, type, arguments)
    }

    suspend inline fun <reified T> publish(
        routingKey: IMessagingKey,
        value: T,
        publishAt: Instant?,
        persistent: Boolean = false,
        attempts: Int = 3,
        delay: Long = 5000,
    ) {
        tryWithAttempts(attempts, delay) {
            val delay = publishAt?.minus(Clock.System.now())?.inWholeMilliseconds ?: 0
            channel!!.basicPublish(
                body = (json ?: Serialization.json).encodeToString(value).toByteArray(),
                exchange = exchange,
                routingKey = routingKey.key,
                properties = Properties(
                    deliveryMode = if (persistent || this.persistent) 2u else 1u,
                    headers = mapOf("x-delay" to Field.Long(delay))
                ),
            )
        }
    }

}
