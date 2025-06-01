package dev.kaccelero.commons.messaging

import com.rabbitmq.client.*
import dev.kaccelero.serializers.Serialization
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.io.IOException

open class MessagingService(
    open val exchange: String,
    open val host: String,
    open val username: String,
    open val password: String,
    open val handleMessagingUseCase: IHandleMessagingUseCase,
    open val keys: List<IMessagingKey>,
    open val json: Json? = null,
    open val listen: Boolean = true,
    open val persistent: Boolean = false,
    open val quorum: Boolean = false,
    open val dead: Boolean = false,
    open val maxXDeathCount: Int = 1,
    open val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : IMessagingService {

    open val sharedQueue = exchange

    open var connection: Connection? = null
    open var channel: Channel? = null

    init {
        coroutineScope.launch { reconnect() }
    }

    open fun connect() {
        connection = ConnectionFactory().apply {
            host = this@MessagingService.host
            username = this@MessagingService.username
            password = this@MessagingService.password
        }.newConnection()

        channel = connection?.createChannel()
        channel?.addShutdownListener { cause ->
            if (!cause.isInitiatedByApplication) reconnect()
        }
        channel?.basicQos(1)

        setup()
    }

    open fun setup() {
        exchangeDeclare(exchange)
        queueDeclare(sharedQueue, exchange)
        keys.filter { !it.isMultiple }.forEach { routingKey ->
            queueBind(sharedQueue, exchange, routingKey.key)
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

    open fun exchangeDeclare(
        name: String,
        type: String = BuiltinExchangeType.DIRECT.type,
        arguments: Map<String, Any> = mapOf(),
    ) {
        channel?.exchangeDeclare(name, type, true, false, arguments)
        exchangeDeclareAdditionalResources(name, type, arguments)
    }

    protected open fun exchangeDeclareAdditionalResources(
        name: String,
        type: String,
        arguments: Map<String, Any>,
    ) {
        if (maxXDeathCount > 1) channel?.exchangeDeclare("$name-dlx", type, true, false, mapOf())
        if (dead) {
            channel?.exchangeDeclare("$name-dead", BuiltinExchangeType.FANOUT, true, false, mapOf())
            channel?.queueDeclare("$name-dead", true, false, false, null)
            channel?.queueBind("$name-dead", "$name-dead", "#", null)
        }
    }

    open fun queueDeclare(
        name: String,
        exchange: String? = null,
        durable: Boolean = true,
        exclusive: Boolean = false,
        autoDelete: Boolean = false,
        arguments: Map<String, Any> = mapOf(),
    ) {
        val dlxArguments =
            if (maxXDeathCount > 1) {
                channel?.queueDeclare(
                    "$name-dlx", durable, exclusive, autoDelete,
                    mapOf(
                        "x-dead-letter-exchange" to (exchange ?: this.exchange),
                        "x-message-ttl" to 5000,
                    )
                )
                mapOf("x-dead-letter-exchange" to "${exchange ?: this.exchange}-dlx")
            } else emptyMap()
        val quorumArguments =
            if (quorum && durable && !exclusive) mapOf("x-queue-type" to "quorum")
            else emptyMap()
        channel?.queueDeclare(name, durable, exclusive, autoDelete, arguments + dlxArguments + quorumArguments)
    }

    open fun queueBind(
        queue: String,
        exchange: String,
        routingKey: String,
        arguments: Map<String, Any> = mapOf(),
    ) {
        channel?.queueBind(queue, exchange, routingKey, arguments)
        if (maxXDeathCount > 1) channel?.queueBind("$queue-dlx", "$exchange-dlx", routingKey, arguments)
    }

    open fun routingKey(key: String): IMessagingKey {
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
        routingKey: IMessagingKey,
        value: T,
        persistent: Boolean = false,
        attempts: Int = 3,
        delay: Long = 5000,
    ) {
        tryWithAttempts(attempts, delay) {
            channel!!.basicPublish(
                exchange,
                routingKey.key,
                AMQP.BasicProperties.Builder()
                    .deliveryMode(if (persistent || this.persistent) 2 else 1)
                    .build(),
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
                            handleMessagingUseCase(this@MessagingService, routingKey, String(delivery.body))
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

    @Suppress("unchecked_cast")
    open suspend fun handleException(delivery: Delivery, exception: Exception) {
        if (maxXDeathCount > 1) {
            val xDeath = delivery.properties.headers?.get("x-death") as? List<Map<String, Any>>
            val retryCount = xDeath?.firstOrNull()?.get("count") as? Long ?: 0
            val tryAgain = retryCount < maxXDeathCount

            // Reject, so it goes to the DLX
            if (tryAgain) channel?.basicReject(delivery.envelope.deliveryTag, false)
            else handleFailedMessage(delivery, exception)
        } else handleFailedMessage(delivery, exception)
    }

    open suspend fun handleFailedMessage(delivery: Delivery, exception: Exception) {
        exception.printStackTrace()

        if (dead) try {
            val deadExchange = delivery.envelope.exchange + "-dead"
            channel?.exchangeDeclarePassive(deadExchange) // Checks if exchange exists or throws an IOException

            val updatedHeaders = (delivery.properties.headers ?: emptyMap<String, Any>()).toMutableMap()
            updatedHeaders["x-failed-reason"] = exception.message ?: "Unknown error"
            updatedHeaders["x-failed-at"] = System.currentTimeMillis()

            val newProps = delivery.properties.builder().headers(updatedHeaders).build()
            channel?.basicPublish(deadExchange, delivery.envelope.routingKey, newProps, delivery.body)
        } catch (_: IOException) {
            // Exchange does not exist, we ignore it
        }

        // Discard the message. Override this method to handle failed messages.
        channel?.basicAck(delivery.envelope.deliveryTag, false)
    }

}
