package dev.kaccelero.models

import dev.kaccelero.serializers.UUIDSerializer
import js.import.JsModule
import js.typedarrays.Uint8Array
import kotlinx.serialization.Serializable
import kotlin.js.JsName

@JsModule("uuid")
@JsNonModule
external object uuid {
    fun v4(): Uint8Array<*>
    fun parse(string: String): Uint8Array<*>
    fun stringify(uuid: Uint8Array<*>): String
}

@Serializable(UUIDSerializer::class)
actual data class UUID(val jsUUID: Uint8Array<*>) {

    @JsName("randomUUID")
    actual constructor() : this(uuid.v4())

    @JsName("fromString")
    actual constructor(string: String) : this(uuid.parse(string))

    actual override fun toString(): String = uuid.stringify(jsUUID).lowercase()

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as UUID
        return jsUUID == other.jsUUID
    }

    actual override fun hashCode(): Int = jsUUID.hashCode()

}
