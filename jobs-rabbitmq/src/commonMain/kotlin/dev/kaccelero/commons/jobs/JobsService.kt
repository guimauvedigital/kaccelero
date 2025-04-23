package dev.kaccelero.commons.jobs

import com.rabbitmq.client.*
import dev.kaccelero.serializers.Serialization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

open class JobsService(
    open val exchange: String,
    open val host: String,
    open val username: String,
    open val password: String,
    open val handleJobUseCase: IHandleJobUseCase,
    open val keys: List<IJobKey>,
    open val json: Json? = null,
    open val listen: Boolean = true,
) : IJobsService {

    val coroutineScope = CoroutineScope(Dispatchers.IO)

    open val sharedQueue = exchange

    open var connection: Connection? = null
    open var channel: Channel? = null

    init {
        coroutineScope.launch { reconnect() }
    }

    open fun connect() {
        connection = ConnectionFactory().apply {
            host = this@JobsService.host
            username = this@JobsService.username
            password = this@JobsService.password
        }.newConnection()

        channel = connection?.createChannel()
        channel?.addShutdownListener { cause ->
            if (!cause.isInitiatedByApplication) reconnect()
        }
        channel?.basicQos(1)

        exchangeDeclare(exchange)
        channel?.queueDeclare(sharedQueue, true, false, false, null)
        keys.filter { !it.isMultiple }.forEach { routingKey ->
            channel?.queueBind(sharedQueue, exchange, routingKey.key)
        }
    }

    @Synchronized
    open fun reconnect() {
        try {
            if (channel?.isOpen == true) channel?.close()
            if (connection?.isOpen == true) connection?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        try {
            connect()
            if (listen) coroutineScope.launch { listen() }
        } catch (_: Exception) {
            coroutineScope.launch {
                delay(5000) // Try again after 5 seconds
                reconnect()
            }
        }
    }

    open fun exchangeDeclare(name: String) {
        channel?.exchangeDeclare(
            name, BuiltinExchangeType.DIRECT, true, false,
            mapOf()
        )
    }

    open fun routingKey(key: String): IJobKey {
        return keys.find { it.key == key }
            ?: throw IllegalArgumentException("Invalid routing key: $key")
    }

    open suspend fun tryWithAttempts(
        attempts: Int = 3,
        delay: Long = 5000,
        block: () -> Unit,
    ) {
        var leftAttempts = attempts
        while (leftAttempts > 0) {
            try {
                block()
                leftAttempts = 0 // Exit loop on success
            } catch (_: Exception) {
                delay(delay) // Try again after delay
                leftAttempts--
            }
        }
    }

    suspend inline fun <reified T> publish(
        routingKey: IJobKey,
        value: T,
        attempts: Int = 3,
        delay: Long = 5000,
    ) {
        tryWithAttempts(attempts, delay) {
            channel!!.basicPublish(
                exchange,
                routingKey.key,
                null,
                (json ?: Serialization.json).encodeToString(value).toByteArray()
            )
        }
    }

    override suspend fun listen() {
        val exclusiveQueue = channel?.queueDeclare()?.queue ?: return
        keys.filter { it.isMultiple }.forEach { routingKey ->
            channel?.queueBind(exclusiveQueue, exchange, routingKey.key)
        }
        listOf(sharedQueue, exclusiveQueue).forEach { queue ->
            channel?.basicConsume(
                queue,
                false,
                { _, delivery ->
                    coroutineScope.launch {
                        try {
                            val routingKey = routingKey(delivery.envelope.routingKey)
                            handleJobUseCase(this@JobsService, routingKey, String(delivery.body))
                            channel?.basicAck(delivery.envelope.deliveryTag, false)
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
        channel?.basicNack(delivery.envelope.deliveryTag, false, exception !is SerializationException)
    }

}
