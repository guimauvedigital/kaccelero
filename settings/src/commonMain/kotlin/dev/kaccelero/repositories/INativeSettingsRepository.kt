package dev.kaccelero.repositories

import dev.kaccelero.models.UUID

interface INativeSettingsRepository {

    // Default storage

    fun remove(key: String)
    fun hasKey(key: String): Boolean

    fun getString(key: String): String?
    fun getBoolean(key: String): Boolean?
    fun getInt(key: String): Int?
    fun getLong(key: String): Long?
    fun getFloat(key: String): Float?
    fun getDouble(key: String): Double?
    fun getUUID(key: String): UUID?

    fun setString(key: String, value: String)
    fun setBoolean(key: String, value: Boolean)
    fun setInt(key: String, value: Int)
    fun setLong(key: String, value: Long)
    fun setFloat(key: String, value: Float)
    fun setDouble(key: String, value: Double)
    fun setUUID(key: String, value: UUID)

    // Secure storage

    fun removeSecure(key: String)
    fun hasSecureKey(key: String): Boolean

    fun getSecureString(key: String): String?
    fun getSecureBoolean(key: String): Boolean?
    fun getSecureInt(key: String): Int?
    fun getSecureLong(key: String): Long?
    fun getSecureFloat(key: String): Float?
    fun getSecureDouble(key: String): Double?
    fun getSecureUUID(key: String): UUID?

    fun setSecureString(key: String, value: String)
    fun setSecureBoolean(key: String, value: Boolean)
    fun setSecureInt(key: String, value: Int)
    fun setSecureLong(key: String, value: Long)
    fun setSecureFloat(key: String, value: Float)
    fun setSecureDouble(key: String, value: Double)
    fun setSecureUUID(key: String, value: UUID)

}
