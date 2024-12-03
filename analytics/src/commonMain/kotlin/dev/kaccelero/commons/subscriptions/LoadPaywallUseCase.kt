package dev.kaccelero.commons.subscriptions

import dev.kaccelero.repositories.ISubscriptionRepository

class LoadPaywallUseCase(
    private val subscriptionRepository: ISubscriptionRepository,
) : ILoadPaywallUseCase {

    override fun invoke(input1: String, input2: () -> Unit) = subscriptionRepository.loadPaywall(input1, input2)

}
