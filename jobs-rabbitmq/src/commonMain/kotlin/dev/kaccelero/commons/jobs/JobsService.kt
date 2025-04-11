package dev.kaccelero.commons.jobs

import com.rabbitmq.client.*
import dev.kaccelero.serializers.Serialization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

open class JobsService(
    open val exchange: String,
    open val host: String,
    open val username: String,
    open val password: String,
    open val handleJobResponseUseCase: IHandleJobResponseUseCase,
    open val keys: List<IJobKey>,
    open val json: Json? = null,
) : IJobsService {

    open val sharedQueue = exchange

    open lateinit var connection: Connection
    open lateinit var channel: Channel

    init {
        try {
            connection = ConnectionFactory().apply {
                host = this@JobsService.host
                username = this@JobsService.username
                password = this@JobsService.password
            }.newConnection()

            channel = connection.createChannel()
            channel.basicQos(1)
            exchangeDeclare(exchange)
            channel.queueDeclare(sharedQueue, true, false, false, null)
            keys.filter { !it.isMultiple }.forEach { routingKey ->
                channel.queueBind(sharedQueue, exchange, routingKey.key)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    open fun exchangeDeclare(name: String) {
        channel.exchangeDeclare(
            name, BuiltinExchangeType.DIRECT, true, false,
            mapOf()
        )
    }

    open fun routingKey(key: String): IJobKey {
        return keys.find { it.key == key }
            ?: throw IllegalArgumentException("Invalid routing key: $key")
    }

    inline fun <reified T> publish(
        routingKey: IJobKey,
        value: T,
    ) {
        channel.basicPublish(
            exchange,
            routingKey.key,
            null,
            (json ?: Serialization.json).encodeToString(value).toByteArray()
        )
    }

    override suspend fun listen() {
        val coroutineScope = CoroutineScope(Dispatchers.IO)
        val exclusiveQueue = channel.queueDeclare().queue
        keys.filter { it.isMultiple }.forEach { routingKey ->
            channel.queueBind(exclusiveQueue, exchange, routingKey.key)
        }
        listOf(sharedQueue, exclusiveQueue).forEach { queue ->
            channel.basicConsume(
                queue,
                false,
                { _, delivery ->
                    coroutineScope.launch {
                        try {
                            val routingKey = routingKey(delivery.envelope.routingKey)
                            handleJobResponseUseCase(this@JobsService, routingKey, String(delivery.body))
                            channel.basicAck(delivery.envelope.deliveryTag, false)
                        } catch (exception: Exception) {
                            handleException(delivery, exception)
                        }
                    }
                },
                { _ -> }
            )
        }
    }

    open fun handleException(delivery: Delivery, exception: Exception) {
        exception.printStackTrace()
        channel.basicNack(delivery.envelope.deliveryTag, false, exception !is SerializationException)
    }

}
