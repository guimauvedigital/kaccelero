package dev.kaccelero.commons.jobs

import com.rabbitmq.client.AMQP
import com.rabbitmq.client.BuiltinExchangeType
import dev.kaccelero.serializers.Serialization
import kotlinx.coroutines.delay
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
) : JobsService(exchange, host, username, password, handleJobUseCase, keys, json, listen) {

    override fun exchangeDeclare(name: String) {
        channel.exchangeDeclare(
            name, "x-delayed-message", true, false,
            mapOf("x-delayed-type" to BuiltinExchangeType.DIRECT.type)
        )
    }

    suspend inline fun <reified T> publish(
        routingKey: IJobKey,
        value: T,
        publishAt: Instant?,
        maxAttempts: Int = 3,
    ) {
        val delay = publishAt?.minus(Clock.System.now())?.inWholeMilliseconds ?: 0
        var leftAttempts = maxAttempts
        while (leftAttempts > 0) {
            try {
                channel.basicPublish(
                    exchange,
                    routingKey.key,
                    AMQP.BasicProperties.Builder().headers(mapOf("x-delay" to delay)).build(),
                    (json ?: Serialization.json).encodeToString(value).toByteArray()
                )
            } catch (_: Exception) {
                delay(5000) // Try again after 5 seconds
                leftAttempts--
            }
        }
    }

}
