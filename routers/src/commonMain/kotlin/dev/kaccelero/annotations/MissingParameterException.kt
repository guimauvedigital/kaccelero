package dev.kaccelero.annotations

data class MissingParameterException(
    val type: ParameterType,
    val key: String?,
) : Exception()
