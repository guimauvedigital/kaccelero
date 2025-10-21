package dev.kaccelero.models

import dev.kaccelero.serializers.UUIDSerializer
import kotlinx.serialization.Serializable


@Serializable(UUIDSerializer::class)
expect class UUID {

    constructor()
    constructor(string: String)

    override fun toString(): String

    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int

}
