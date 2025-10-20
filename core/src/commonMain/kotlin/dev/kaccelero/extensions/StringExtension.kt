package dev.kaccelero.extensions

import dev.kaccelero.models.UUID

fun String.toUUID(): UUID = UUID(this)

fun String.toUUIDOrNull(): UUID? = try {
    toUUID()
} catch (_: Exception) {
    null
}

/**
 * Normalizes a UUID string by adding dashes if they are missing.
 * Supports both formats:
 * - With dashes: 7e6ae51b-4aab-4f5e-97f5-83ecb6dcf16f
 * - Without dashes: 7E6AE51B4AAB4F5E97F583ECB6DCF16F
 */
fun String.normalizeUUID(): String {
    val cleaned = replace("-", "").lowercase()

    // Validate length (32 hex characters)
    require(cleaned.length == 32) { "UUID string must have 32 hex characters, got ${cleaned.length}" }

    // Validate hex characters
    require(cleaned.all { it in '0'..'9' || it in 'a'..'f' }) {
        "UUID string contains invalid characters: $this"
    }

    // Insert dashes at standard positions: 8-4-4-4-12
    return buildString {
        append(cleaned.substring(0, 8))
        append('-')
        append(cleaned.substring(8, 12))
        append('-')
        append(cleaned.substring(12, 16))
        append('-')
        append(cleaned.substring(16, 20))
        append('-')
        append(cleaned.substring(20, 32))
    }
}
