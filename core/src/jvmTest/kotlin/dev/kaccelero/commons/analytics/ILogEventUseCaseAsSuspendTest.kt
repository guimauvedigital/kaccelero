package dev.kaccelero.commons.analytics

import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.runBlocking
import kotlin.test.Test

class ILogEventUseCaseAsSuspendTest {

    @Test
    fun invokeSuspendFromNonSuspend() = runBlocking {
        val useCase = mockk<ILogEventUseCase>()
        val suspendUseCase = ILogEventUseCaseAsSuspend(useCase)
        every {
            useCase(
                AnalyticsEventName("test"), listOf(
                    AnalyticsEventParameter("test") to AnalyticsEventValue("test")
                )
            )
        } returns Unit
        suspendUseCase(
            AnalyticsEventName("test"), listOf(
                AnalyticsEventParameter("test") to AnalyticsEventValue("test")
            )
        )
        verify {
            useCase(
                AnalyticsEventName("test"), listOf(
                    AnalyticsEventParameter("test") to AnalyticsEventValue("test")
                )
            )
        }
    }

}
