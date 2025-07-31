package dev.kaccelero.commons.analytics

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsUserProperty(override val key: String) : IAnalyticsUserProperty
