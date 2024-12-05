package dev.kaccelero.commons.analytics

import kotlinx.serialization.Serializable
import kotlin.js.JsExport

@JsExport
@Serializable
data class AnalyticsUserValue<T>(val value: T) : IAnalyticsUserValue {

    override val stringValue: String
        get() = value.toString()

}
