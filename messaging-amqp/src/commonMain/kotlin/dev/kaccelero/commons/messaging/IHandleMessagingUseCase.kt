package dev.kaccelero.commons.messaging

import dev.kaccelero.usecases.ITripleSuspendUseCase

interface IHandleMessagingUseCase : ITripleSuspendUseCase<IMessagingService, IMessagingKey, String, Unit>
