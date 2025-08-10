package dev.kaccelero.commons.messaging

import dev.kaccelero.serializers.Serialization
import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.BuiltinExchangeType
import dev.kourier.amqp.Field
import dev.kourier.amqp.channel.AMQPChannel
import dev.kourier.amqp.connection.AMQPConnection
import dev.kourier.amqp.properties
import dev.kourier.amqp.robust.createRobustAMQPConnection
import io.ktor.utils.io.core.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

open class MessagingService(
    open val host: String,
    open val user: String,
    open val password: String,
    open val exchange: IMessagingExchange,
    open val queue: IMessagingQueue,
    open val keys: List<IMessagingKey>,
    open val handleMessagingUseCaseFactory: () -> IHandleMessagingUseCase,
    open val coroutineScope: CoroutineScope,
    open val json: Json? = null,
    open val autoConnect: Boolean = true,
    open val autoListen: Boolean = true,
    open val persistent: Boolean = false,
    open val quorum: Boolean = false,
    open val dead: Boolean = false,
    open val maxXDeathCount: Int = 1,
    open val connectionName: String = queue.queue,
) : IMessagingService {

    override var connection: AMQPConnection? = null
    override var channel: AMQPChannel? = null

    init {
        coroutineScope.launch {
            if (autoConnect) connect()
            if (autoConnect && autoListen) listen()
        }
    }

    override suspend fun connect() {
        connection = createRobustAMQPConnection(coroutineScope) {
            server {
                host = this@MessagingService.host
                user = this@MessagingService.user
                password = this@MessagingService.password
                connectionName = this@MessagingService.connectionName
            }
        }

        channel = connection?.openChannel()
        channel?.basicQos(1u)

        setup()
    }

    override suspend fun setup() {
        exchangeDeclare(exchange)
        queueDeclare(queue, exchange)
        keys.filter { !it.isMultiple }.forEach { routingKey ->
            queueBind(queue, exchange, routingKey)
        }
    }

    override suspend fun exchangeDeclare(
        exchange: IMessagingExchange,
        type: String,
        arguments: Map<String, Field>,
    ) {
        channel?.exchangeDeclare(
            name = exchange.exchange,
            type = type,
            durable = true,
            arguments = arguments
        )
        exchangeDeclareAdditionalResources(exchange, type, arguments)
    }

    protected open suspend fun exchangeDeclareAdditionalResources(
        exchange: IMessagingExchange,
        type: String,
        arguments: Map<String, Field>,
    ) {
        if (maxXDeathCount > 1) {
            channel?.exchangeDeclare(
                name = "${exchange.exchange}-dlx",
                type = BuiltinExchangeType.FANOUT,
                durable = true,
            )
            channel?.queueDeclare(
                name = "${exchange.exchange}-dlx",
                durable = true,
                arguments = mapOf(
                    "x-dead-letter-exchange" to Field.LongString(exchange.exchange),
                    "x-message-ttl" to Field.Int(5000),
                )
            )
            if (maxXDeathCount > 1) channel?.queueBind(
                queue = "${exchange.exchange}-dlx",
                exchange = "${exchange.exchange}-dlx",
                routingKey = "#"
            )
        }
        if (dead) {
            channel?.exchangeDeclare(
                name = "${exchange.exchange}-dead",
                type = BuiltinExchangeType.FANOUT,
                durable = true,
            )
            channel?.queueDeclare(
                name = "${exchange.exchange}-dead",
                durable = true,
            )
            channel?.queueBind(
                queue = "${exchange.exchange}-dead",
                exchange = "${exchange.exchange}-dead",
                routingKey = "#"
            )
        }
    }

    override suspend fun queueDeclare(
        queue: IMessagingQueue,
        exchange: IMessagingExchange?,
        durable: Boolean,
        exclusive: Boolean,
        autoDelete: Boolean,
        arguments: Map<String, Field>,
    ) {
        val dlxArguments =
            if (maxXDeathCount > 1) mapOf("x-dead-letter-exchange" to Field.LongString("${(exchange ?: this.exchange).exchange}-dlx"))
            else emptyMap()
        val quorumArguments =
            if (quorum && durable && !exclusive) mapOf("x-queue-type" to Field.LongString("quorum"))
            else emptyMap()
        channel?.queueDeclare(
            name = queue.queue,
            durable = durable,
            exclusive = exclusive,
            autoDelete = autoDelete,
            arguments = arguments + dlxArguments + quorumArguments
        )
    }

    override suspend fun queueBind(
        queue: IMessagingQueue,
        exchange: IMessagingExchange,
        routingKey: IMessagingKey,
        arguments: Map<String, Field>,
    ) {
        channel?.queueBind(
            queue = queue.queue,
            exchange = exchange.exchange,
            routingKey = routingKey.key,
            arguments = arguments
        )
    }

    open fun routingKey(key: String): IMessagingKey {
        return keys.find { it.key == key }
            ?: throw IllegalArgumentException("Invalid routing key: $key")
    }

    // This might not be required anymore? (with robust connection)
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
        exchange: IMessagingExchange? = null,
        persistent: Boolean = false,
        attempts: Int = 3,
        delay: Long = 5000,
    ) {
        tryWithAttempts(attempts, delay) {
            channel!!.basicPublish(
                body = (json ?: Serialization.json).encodeToString(value).toByteArray(),
                exchange = (exchange ?: this.exchange).exchange,
                routingKey = routingKey.key,
                properties = properties {
                    deliveryMode = if (persistent || this@MessagingService.persistent) 2u else 1u
                },
            )
        }
    }

    override suspend fun listen() {
        val handleMessagingUseCase = handleMessagingUseCaseFactory()
        val exclusiveQueue = channel?.queueDeclare()?.queueName ?: return
        keys.filter { it.isMultiple }.forEach { routingKey ->
            channel?.queueBind(exclusiveQueue, exchange.exchange, routingKey.key)
        }
        listOf(queue.queue, exclusiveQueue).forEach { queue ->
            channel?.basicConsume(
                queue,
                noAck = false,
                onDelivery = { delivery ->
                    try {
                        val routingKey = routingKey(delivery.message.routingKey)
                        handleMessagingUseCase(routingKey, delivery.message.body.decodeToString())
                        channel?.basicAck(delivery.message.deliveryTag, false)
                    } catch (exception: Exception) {
                        handleException(delivery, exception)
                    }
                }
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

        if (dead) runCatching { // if exchange does not exist, we ignore it
            val tempChannel = connection?.openChannel() ?: return@runCatching

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
        }

        // Discard the message. Override this method to handle failed messages.
        channel?.basicAck(delivery.message.deliveryTag, false)
    }

}
