package dev.kaccelero.extensions

import dev.kaccelero.models.UUID

fun String.toUUID(): UUID = UUID(this)

fun String.toUUIDOrNull(): UUID? = try {
    toUUID()
} catch (_: Exception) {
    null
}
