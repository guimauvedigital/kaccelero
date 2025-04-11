package dev.kaccelero.mocks

import kotlinx.serialization.Serializable

@Serializable
data class TestGenericPayload<T>(
    val value: T,
)
