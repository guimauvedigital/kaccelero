package dev.kaccelero.commons.analytics

import dev.kaccelero.commons.application.IAskForReviewUseCase
import dev.kaccelero.repositories.IAnalyticsRepository
import dev.kaccelero.repositories.ISubscriptionRepository

class LogEventWithCompletionUseCase(
    private val analyticsRepository: IAnalyticsRepository,
    private val subscriptionRepository: ISubscriptionRepository,
    private val askForReviewUseCase: IAskForReviewUseCase? = null,
) : ILogEventWithCompletionUseCase {

    override fun invoke(
        input1: IAnalyticsEventName,
        input2: Map<IAnalyticsEventParameter, IAnalyticsEventValue>,
        input3: () -> Unit,
    ) {
        val wasSubscribed = subscriptionRepository.getSubscriptionStatus().isActive
        analyticsRepository.logEvent(input1, input2) {
            if (!wasSubscribed && subscriptionRepository.getSubscriptionStatus().isActive) askForReviewUseCase?.invoke()
            input3()
        }
    }

}
