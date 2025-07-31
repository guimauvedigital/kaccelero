package dev.kaccelero.repositories

import kotlinx.serialization.Serializable

@Serializable
data class Pagination(
    val limit: Long,
    val offset: Long,
    val options: IPaginationOptions? = null,
)
