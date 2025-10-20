package dev.kaccelero.models

import dev.kaccelero.extensions.normalizeUUID
import dev.kaccelero.serializers.UUIDSerializer
import kotlinx.serialization.Serializable

@Serializable(UUIDSerializer::class)
actual class UUID(val javaUUID: java.util.UUID) {

    actual constructor() : this(java.util.UUID.randomUUID())
    actual constructor(string: String) : this(java.util.UUID.fromString(string.normalizeUUID()))

    actual override fun toString(): String = javaUUID.toString()

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as UUID
        return javaUUID == other.javaUUID
    }

    actual override fun hashCode(): Int = javaUUID.hashCode()

}
