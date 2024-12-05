package dev.kaccelero.commons.analytics

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class AnalyticsEventParameter(override val key: String) : IAnalyticsEventParameter
