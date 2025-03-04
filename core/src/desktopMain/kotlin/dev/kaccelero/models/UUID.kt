package dev.kaccelero.models

import dev.kaccelero.serializers.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable(UUIDSerializer::class)
actual class UUID private constructor(private val bytes: ByteArray) {

    actual constructor() : this(Random.nextBytes(UUID_BYTES).setVersion(4))
    actual constructor(string: String) : this(uuidBytesFromString(string))

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false
        other as UUID
        return bytes.contentEquals(other.bytes)
    }

    actual override fun hashCode(): Int = bytes.contentHashCode()

    actual override fun toString(): String {
        val characters = CharArray(UUID_BYTES)
        var charIndex = 0
        for (range in UUID_BYTE_RANGES) {
            for (i in range) {
                val octetPair = bytes[i]
                val left = octetPair.toInt().shr(4) and 0b00001111
                val right = octetPair.toInt() and 0b00001111
                characters[charIndex++] = UUID_CHARS[left]
                characters[charIndex++] = UUID_CHARS[right]
            }
            if (charIndex < UUID_BYTES) characters[charIndex++] = '-'
        }
        return characters.concatToString()
    }

}
