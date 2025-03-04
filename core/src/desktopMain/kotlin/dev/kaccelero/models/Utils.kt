package dev.kaccelero.models

internal const val UUID_BYTES = 16
internal const val UUID_STRING_LENGTH = 36
internal val UUID_CHARS = ('0'..'9') + ('a'..'f')
internal val UUID_CHAR_RANGES: List<IntRange> = listOf(
    0 until 8,
    9 until 13,
    14 until 18,
    19 until 23,
    24 until 36,
)
internal val UUID_BYTE_RANGES: List<IntRange> = listOf(
    0 until 4,
    4 until 6,
    6 until 8,
    8 until 10,
    10 until 16,
)

internal fun uuidBytesFromString(string: String): ByteArray {
    require(string.length == UUID_STRING_LENGTH) { "UUID string has invalid length: $string" }
    val bytes = ByteArray(UUID_BYTES)
    var byte = 0
    for (range in UUID_CHAR_RANGES) {
        var i = range.first
        while (i < range.last) {
            val left = halfByteFromChar(string[i++])
            val right = halfByteFromChar(string[i++])
            require(left != null && right != null) {
                "Uuid string has invalid characters: $string"
            }

            // smash them together into a single byte
            bytes[byte++] = (left.shl(4) or right).toByte()
        }
    }
    return bytes
}

internal fun halfByteFromChar(char: Char) = when (char) {
    in '0'..'9' -> char.code - 48
    in 'a'..'f' -> char.code - 87
    in 'A'..'F' -> char.code - 55
    else -> null
}

internal inline fun ByteArray.setVersion(version: Int) = apply {
    this[6] = ((this[6].toInt() and 0x0F) or (version shl 4)).toByte()
    this[8] = ((this[8].toInt() and 0x3F) or 0x80).toByte()
}
