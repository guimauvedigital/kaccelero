package dev.kaccelero.models

import dev.kaccelero.extensions.normalizeUUID
import dev.kaccelero.serializers.UUIDSerializer
import js.typedarrays.Uint8Array
import kotlinx.serialization.Serializable

@JsModule("uuid")
@JsNonModule
external val uuid: dynamic

@JsExport
@Serializable(UUIDSerializer::class)
actual data class UUID(val jsUUID: Uint8Array<*>) {

    @JsName("randomUUID")
    actual constructor() : this(uuid.v4(null, js("new Uint8Array(16)")) as Uint8Array<*>)

    @JsName("fromString")
    actual constructor(string: String) : this(uuid.parse(string.normalizeUUID()) as Uint8Array<*>)

    actual override fun toString(): String = (uuid.stringify(jsUUID) as String).lowercase()

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        if (other !is UUID) return false
        return toString() == other.toString()
    }

    actual override fun hashCode(): Int = jsUUID.hashCode()

}
