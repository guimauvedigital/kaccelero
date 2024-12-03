package dev.kaccelero.commons.subscriptions

import kotlin.js.JsExport

@JsExport
interface SubscriptionStatus {

    val isActive: Boolean

}
