package dev.kaccelero.commons.jobs

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.BuiltinExchangeType
import dev.kaccelero.serializers.Serialization
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json

open class DelayedJobsService(
    exchange: String,
    host: String,
    username: String,
    password: String,
    handleJobUseCase: IHandleJobUseCase,
    keys: List<IJobKey>,
    json: Json? = null,
    listen: Boolean = true,
    persistent: Boolean = false,
) : JobsService(exchange, host, username, password, handleJobUseCase, keys, json, listen, persistent) {

    override fun exchangeDeclare(name: String) {
        channel?.exchangeDeclare(
            name, "x-delayed-message", true, false,
            mapOf("x-delayed-type" to BuiltinExchangeType.DIRECT.type)
        )
    }

    suspend inline fun <reified T> publish(
        routingKey: IJobKey,
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
