package dev.kaccelero.commons.analytics

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class AnalyticsEventName(override val key: String) : IAnalyticsEventName
