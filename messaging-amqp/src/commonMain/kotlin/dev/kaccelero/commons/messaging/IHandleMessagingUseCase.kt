package dev.kaccelero.commons.messaging

import dev.kaccelero.usecases.ITripleSuspendUseCase
import dev.kourier.amqp.AMQPResponse

interface IHandleMessagingUseCase :
    ITripleSuspendUseCase<IMessagingKey, String, AMQPResponse.Channel.Message.Delivery, Unit>
