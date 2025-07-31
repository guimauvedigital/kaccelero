package dev.kaccelero.commons.analytics

import kotlinx.serialization.Serializable

@Serializable
data class AnalyticsUserValue<T>(val value: T) : IAnalyticsUserValue {

    override val stringValue: String
        get() = value.toString()

}
