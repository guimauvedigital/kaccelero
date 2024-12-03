package dev.kaccelero.commons.analytics

import kotlin.js.JsExport

@JsExport
data class AnalyticsEventParameter(override val key: String) : IAnalyticsEventParameter
