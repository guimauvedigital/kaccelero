package dev.kaccelero.commons.messaging

import kotlinx.serialization.Serializable

@Serializable
data class TestMessage(
    val message: String,
)
