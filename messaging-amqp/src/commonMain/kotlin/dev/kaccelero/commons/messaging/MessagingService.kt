package dev.kaccelero.commons.messaging

import dev.kaccelero.serializers.Serialization
import dev.kourier.amqp.*
import dev.kourier.amqp.channel.AMQPChannel
import dev.kourier.amqp.connection.AMQPConnection
import dev.kourier.amqp.connection.amqpConfig
import dev.kourier.amqp.connection.createAMQPConnection
import io.ktor.utils.io.core.*
import kotlinx.coroutines.*
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

open class MessagingService(
    open val exchange: String,
    open val host: String,
    open val user: String,
    open val password: String,
    open val handleMessagingUseCase: IHandleMessagingUseCase,
    open val keys: List<IMessagingKey>,
    open val json: Json? = null,
    open val listen: Boolean = true,
    open val persistent: Boolean = false,
    open val quorum: Boolean = false,
    open val dead: Boolean = false,
    open val maxXDeathCount: Int = 1,
    open val connectionName: String = exchange,
    open val coroutineScope: CoroutineScope = CoroutineScope(Dispatchers.IO),
) : IMessagingService {

    open val sharedQueue = exchange

    open var connection: AMQPConnection? = null
    open var channel: AMQPChannel? = null

    init {
        coroutineScope.launch { reconnect() }
    }

    open suspend fun connect() {
        connection = createAMQPConnection(coroutineScope, amqpConfig {
            server {
                host = this@MessagingService.host
                user = this@MessagingService.user
                password = this@MessagingService.password
                connectionName = this@MessagingService.connectionName
            }
        })

        channel = connection?.openChannel()
        coroutineScope.launch {
            val cause = connection?.connectionClosed?.await() ?: return@launch
            if (!cause.isInitiatedByApplication) reconnect()
        }
        channel?.basicQos(1u)

        setup()
    }

    open suspend fun setup() {
        exchangeDeclare(exchange)
        queueDeclare(sharedQueue, exchange)
        keys.filter { !it.isMultiple }.forEach { routingKey ->
            queueBind(sharedQueue, exchange, routingKey.key)
        }
    }

    open suspend fun reconnect() {
        runCatching {
            channel?.close()
            connection?.close()
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

    open suspend fun exchangeDeclare(
        name: String,
        type: String = BuiltinExchangeType.DIRECT,
        arguments: Map<String, Field> = mapOf(),
    ) {
        channel?.exchangeDeclare(
            name = name,
            type = type,
            durable = true,
            autoDelete = false,
            arguments = arguments
        )
        exchangeDeclareAdditionalResources(name, type, arguments)
    }

    protected open suspend fun exchangeDeclareAdditionalResources(
        name: String,
        type: String,
        arguments: Map<String, Field>,
    ) {
        if (maxXDeathCount > 1) channel?.exchangeDeclare(
            name = "$name-dlx",
            type = type,
            durable = true,
            autoDelete = false
        )
        if (dead) {
            channel?.exchangeDeclare(
                name = "$name-dead",
                type = BuiltinExchangeType.FANOUT,
                durable = true,
                autoDelete = false
            )
            channel?.queueDeclare(
                name = "$name-dead",
                durable = true,
                exclusive = false,
                autoDelete = false,
            )
            channel?.queueBind(
                queue = "$name-dead",
                exchange = "$name-dead",
                routingKey = "#"
            )
        }
    }

    open suspend fun queueDeclare(
        name: String,
        exchange: String? = null,
        durable: Boolean = true,
        exclusive: Boolean = false,
        autoDelete: Boolean = false,
        arguments: Map<String, Field> = mapOf(),
    ) {
        val dlxArguments =
            if (maxXDeathCount > 1) {
                channel?.queueDeclare(
                    name = "$name-dlx",
                    durable = durable,
                    exclusive = exclusive,
                    autoDelete = autoDelete,
                    arguments = mapOf(
                        "x-dead-letter-exchange" to Field.LongString(exchange ?: this.exchange),
                        "x-message-ttl" to Field.Int(5000),
                    )
                )
                mapOf("x-dead-letter-exchange" to Field.LongString("${exchange ?: this.exchange}-dlx"))
            } else emptyMap()
        val quorumArguments =
            if (quorum && durable && !exclusive) mapOf("x-queue-type" to Field.LongString("quorum"))
            else emptyMap()
        channel?.queueDeclare(
            name = name,
            durable = durable,
            exclusive = exclusive,
            autoDelete = autoDelete,
            arguments = arguments + dlxArguments + quorumArguments
        )
    }

    open suspend fun queueBind(
        queue: String,
        exchange: String,
        routingKey: String,
        arguments: Map<String, Field> = mapOf(),
    ) {
        channel?.queueBind(
            queue = queue,
            exchange = exchange,
            routingKey = routingKey,
            arguments = arguments
        )
        if (maxXDeathCount > 1) channel?.queueBind(
            queue = "$queue-dlx",
            exchange = "$exchange-dlx",
            routingKey = routingKey,
            arguments = arguments
        )
    }

    open fun routingKey(key: String): IMessagingKey {
        return keys.find { it.key == key }
            ?: throw IllegalArgumentException("Invalid routing key: $key")
    }

    open suspend fun tryWithAttempts(
        attempts: Int = 3,
        delay: Long = 5000,
        block: suspend () -> Unit,
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
                body = (json ?: Serialization.json).encodeToString(value).toByteArray(),
                exchange = exchange,
                routingKey = routingKey.key,
                properties = Properties(
                    deliveryMode = if (persistent || this.persistent) 2u else 1u
                ),
            )
        }
    }

    override suspend fun listen() {
        val exclusiveQueue = channel?.queueDeclare()?.queueName ?: return
        keys.filter { it.isMultiple }.forEach { routingKey ->
            channel?.queueBind(exclusiveQueue, exchange, routingKey.key)
        }
        listOf(sharedQueue, exclusiveQueue).forEach { queue ->
            channel?.basicConsume(
                queue,
                noAck = false,
                onDelivery = { delivery ->
                    coroutineScope.launch {
                        try {
                            val routingKey = routingKey(delivery.message.routingKey)
                            handleMessagingUseCase(
                                this@MessagingService,
                                routingKey,
                                delivery.message.body.decodeToString()
                            )
                            channel?.basicAck(delivery.message.deliveryTag, false)
                        } catch (exception: Exception) {
                            handleException(delivery, exception)
                        }
                    }
                },
            )
        }
    }

    @Suppress("unchecked_cast")
    open suspend fun handleException(delivery: AMQPResponse.Channel.Message.Delivery, exception: Exception) {
        if (maxXDeathCount > 1) {
            val xDeathArray = delivery.message.properties.headers?.get("x-death") as? Field.Array
            val xDeath = xDeathArray?.value?.firstOrNull() as? Field.Table
            val retryCount = (xDeath?.value?.get("count") as? Field.Long)?.value ?: 0
            val tryAgain = retryCount < maxXDeathCount

            // Reject, so it goes to the DLX
            if (tryAgain) channel?.basicReject(delivery.message.deliveryTag, false)
            else handleFailedMessage(delivery, exception)
        } else handleFailedMessage(delivery, exception)
    }

    open suspend fun handleFailedMessage(delivery: AMQPResponse.Channel.Message.Delivery, exception: Exception) {
        exception.printStackTrace()

        if (dead) try {
            val tempChannel = connection?.openChannel() ?: return

            val deadExchange = delivery.message.exchange + "-dead"
            tempChannel.exchangeDeclarePassive(deadExchange) // Checks if exchange exists or throws an AMQPException

            val updatedHeaders = (delivery.message.properties.headers ?: emptyMap()).toMutableMap()
            updatedHeaders["x-failed-reason"] = Field.LongString(exception.message ?: "Unknown error")
            updatedHeaders["x-failed-at"] = Field.Long(Clock.System.now().toEpochMilliseconds())

            val newProps = delivery.message.properties.copy(headers = updatedHeaders)
            tempChannel.basicPublish(
                body = delivery.message.body,
                exchange = deadExchange,
                routingKey = delivery.message.routingKey,
                properties = newProps
            )

            tempChannel.close()
        } catch (_: AMQPException) {
            // Exchange does not exist, we ignore it
        }

        // Discard the message. Override this method to handle failed messages.
        channel?.basicAck(delivery.message.deliveryTag, false)
    }

}
