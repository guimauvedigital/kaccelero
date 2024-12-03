package dev.kaccelero.commons.subscriptions

import dev.kaccelero.repositories.ISubscriptionRepository

class GetSubscriptionStatusUseCase(
    private val subscriptionRepository: ISubscriptionRepository,
) : IGetSubscriptionStatusUseCase {

    override fun invoke(): SubscriptionStatus = subscriptionRepository.getSubscriptionStatus()

}
