package dev.kaccelero.commons.analytics

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsEventValue<T>(val value: T) : IAnalyticsEventValue {

    override val stringValue: String
        get() = value.toString()

}
