package dev.kaccelero.repositories

import dev.kaccelero.commons.subscriptions.SubscriptionStatus

interface ISubscriptionRepository {

    fun getSubscriptionStatus(): SubscriptionStatus
    fun loadPaywall(placementId: String, completion: () -> Unit)
    fun showPaywall(placementId: String, event: String, completion: () -> Unit)

}
