package dev.kaccelero.commons.messaging

import dev.kaccelero.usecases.IPairSuspendUseCase

interface IHandleMessagingUseCase : IPairSuspendUseCase<IMessagingKey, String, Unit>
