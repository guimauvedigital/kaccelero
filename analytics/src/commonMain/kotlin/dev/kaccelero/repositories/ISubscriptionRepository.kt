package dev.kaccelero.repositories

import dev.kaccelero.commons.subscriptions.ISubscriptionStatus

interface ISubscriptionRepository {

    fun getSubscriptionStatus(): ISubscriptionStatus
    fun loadPaywall(placementId: String, completion: () -> Unit)
    fun showPaywall(placementId: String, event: String, completion: () -> Unit)

}
