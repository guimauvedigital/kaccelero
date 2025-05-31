package dev.kaccelero.commons.messaging

import kotlinx.coroutines.CoroutineScope

interface IMessagingService {

    suspend fun listen(executeInScope: CoroutineScope? = null)

}
