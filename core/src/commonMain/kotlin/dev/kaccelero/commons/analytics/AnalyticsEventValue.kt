package dev.kaccelero.commons.analytics

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class AnalyticsEventValue<T>(val value: T) : IAnalyticsEventValue {

    override val stringValue: String
        get() = value.toString()

}
