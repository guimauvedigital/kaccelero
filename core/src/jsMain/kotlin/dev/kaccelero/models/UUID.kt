package dev.kaccelero.models

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
    actual constructor() : this(uuid.v4() as Uint8Array<*>)

    @JsName("fromString")
    actual constructor(string: String) : this(uuid.parse(string) as Uint8Array<*>)

    actual override fun toString(): String = uuid.stringify(jsUUID).lowercase()

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as UUID
        return jsUUID == other.jsUUID
    }

    actual override fun hashCode(): Int = jsUUID.hashCode()

}
