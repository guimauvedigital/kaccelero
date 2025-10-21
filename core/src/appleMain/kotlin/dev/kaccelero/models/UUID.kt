package dev.kaccelero.models

import dev.kaccelero.extensions.normalizeUUID
import dev.kaccelero.serializers.UUIDSerializer
import kotlinx.cinterop.UnsafeNumber
import kotlinx.serialization.Serializable
import platform.Foundation.NSUUID

@Serializable(UUIDSerializer::class)
actual class UUID(val nsUUID: NSUUID) {

    actual constructor() : this(NSUUID())
    actual constructor(string: String) : this(NSUUID(string.normalizeUUID()))

    actual override fun toString(): String = nsUUID.UUIDString.lowercase()

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as UUID
        return nsUUID == other.nsUUID
    }

    @OptIn(UnsafeNumber::class)
    actual override fun hashCode(): Int = nsUUID.hash.toInt()

}
