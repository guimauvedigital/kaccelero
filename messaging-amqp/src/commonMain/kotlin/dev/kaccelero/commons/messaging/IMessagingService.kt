package dev.kaccelero.commons.messaging

import dev.kourier.amqp.BuiltinExchangeType
import dev.kourier.amqp.Field
import dev.kourier.amqp.channel.AMQPChannel
import dev.kourier.amqp.connection.AMQPConnection

interface IMessagingService {

    val connection: AMQPConnection?
    val channel: AMQPChannel?

    suspend fun connect()
    suspend fun setup()
    suspend fun listen()

    suspend fun exchangeDeclare(
        exchange: IMessagingExchange,
        type: String = BuiltinExchangeType.DIRECT,
        arguments: Map<String, Field> = mapOf(),
    )

    suspend fun queueDeclare(
        queue: IMessagingQueue,
        exchange: IMessagingExchange? = null,
        durable: Boolean = true,
        exclusive: Boolean = false,
        autoDelete: Boolean = false,
        arguments: Map<String, Field> = mapOf(),
    )

    suspend fun queueBind(
        queue: IMessagingQueue,
        exchange: IMessagingExchange,
        routingKey: IMessagingKey,
        arguments: Map<String, Field> = mapOf(),
    )

}
