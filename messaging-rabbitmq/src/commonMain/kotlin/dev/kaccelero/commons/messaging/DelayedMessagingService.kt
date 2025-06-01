package dev.kaccelero.commons.messaging

import com.rabbitmq.client.AMQP
import dev.kaccelero.serializers.Serialization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

open class DelayedMessagingService(
    exchange: String,
    host: String,
    username: String,
    password: String,
    handleMessagingUseCase: IHandleMessagingUseCase,
    keys: List<IMessagingKey>,
    json: Json? = null,
    listen: Boolean = true,
    persistent: Boolean = false,
    quorum: Boolean = false,
    dead: Boolean = false,
    maxXDeathCount: Int = 1,
    coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : MessagingService(
    exchange,
    host,
    username,
    password,
    handleMessagingUseCase,
    keys,
    json,
    listen,
    persistent,
    quorum,
    dead,
    maxXDeathCount,
    coroutineScope
) {

    override fun exchangeDeclare(name: String, type: String, arguments: Map<String, Any>) {
        channel?.exchangeDeclare(name, "x-delayed-message", true, false, arguments + mapOf("x-delayed-type" to type))
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
                exchange,
                routingKey.key,
                AMQP.BasicProperties.Builder()
                    .deliveryMode(if (persistent || this.persistent) 2 else 1)
                    .headers(mapOf("x-delay" to delay))
                    .build(),
                (json ?: Serialization.json).encodeToString(value).toByteArray()
            )
        }
    }

}
