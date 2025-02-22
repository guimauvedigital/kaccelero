package dev.kaccelero.annotations

enum class ParameterType(
    val isBadRequest: Boolean = false,
) {

    ID, PATH, QUERY(true)

}
