package dev.kaccelero.commons.messaging

import dev.kourier.amqp.BuiltinExchangeType
import dev.kourier.amqp.Field
import dev.kourier.amqp.channel.AMQPChannel
import dev.kourier.amqp.connection.AMQPConnection

/**
 * Interface defining the contract for a messaging service.
 */
interface IMessagingService {

    /**
     * The raw AMQP connection instance.
     */
    val connection: AMQPConnection?

    /**
     * The raw AMQP channel instance.
     */
    val channel: AMQPChannel?

    /**
     * Connects to the messaging server.
     */
    suspend fun connect()

    /**
     * Sets up the necessary exchanges, queues, and bindings.
     */
    suspend fun setup()

    /**
     * Starts listening for incoming messages in a coroutine.
     */
    suspend fun listen()

    /**
     * Declares an exchange with the specified parameters.
     *
     * @param exchange The exchange to declare.
     * @param type The type of the exchange (default is DIRECT).
     * @param arguments Additional arguments for the exchange.
     */
    suspend fun exchangeDeclare(
        exchange: IMessagingExchange,
        type: String = BuiltinExchangeType.DIRECT,
        arguments: Map<String, Field> = mapOf(),
    )

    /**
     * Declares a queue with the specified parameters.
     *
     * @param queue The queue to declare.
     * @param exchange The exchange to bind the queue to (optional).
     * @param durable Whether the queue should be durable (default is true).
     * @param exclusive Whether the queue should be exclusive (default is false).
     * @param autoDelete Whether the queue should be auto-deleted (default is false).
     * @param arguments Additional arguments for the queue.
     */
    suspend fun queueDeclare(
        queue: IMessagingQueue,
        exchange: IMessagingExchange? = null,
        durable: Boolean = true,
        exclusive: Boolean = false,
        autoDelete: Boolean = false,
        arguments: Map<String, Field> = mapOf(),
    )

    /**
     * Binds a queue to an exchange with the specified routing key and arguments.
     *
     * @param queue The queue to bind.
     * @param exchange The exchange to bind the queue to.
     * @param routingKey The routing key for the binding.
     * @param arguments Additional arguments for the binding.
     */
    suspend fun queueBind(
        queue: IMessagingQueue,
        exchange: IMessagingExchange,
        routingKey: IMessagingKey,
        arguments: Map<String, Field> = mapOf(),
    )

}
