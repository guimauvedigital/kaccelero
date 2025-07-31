package dev.kaccelero.commons.analytics

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsEventParameter(override val key: String) : IAnalyticsEventParameter
