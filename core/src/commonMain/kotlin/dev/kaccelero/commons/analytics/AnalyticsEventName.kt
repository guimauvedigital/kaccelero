package dev.kaccelero.commons.analytics

import kotlin.js.JsExport

@JsExport
data class AnalyticsEventName(override val name: String) : IAnalyticsEventName
