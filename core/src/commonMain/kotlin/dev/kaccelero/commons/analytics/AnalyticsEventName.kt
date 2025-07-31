package dev.kaccelero.commons.analytics

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsEventName(override val key: String) : IAnalyticsEventName
