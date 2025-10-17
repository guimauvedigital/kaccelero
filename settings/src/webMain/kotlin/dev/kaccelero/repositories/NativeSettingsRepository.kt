package dev.kaccelero.repositories

import dev.kaccelero.models.UUID
import kotlinx.browser.localStorage

class NativeSettingsRepository : INativeSettingsRepository {

    // Default storage

    override fun remove(key: String) {
        localStorage.removeItem(key)
    }

    override fun hasKey(key: String): Boolean =
        localStorage.getItem(key) != null

    override fun getString(key: String): String? =
        localStorage.getItem(key)?.takeIf { it != "null" }

    override fun getBoolean(key: String): Boolean? =
        getString(key)?.toBooleanStrictOrNull()

    override fun getInt(key: String): Int? =
        getString(key)?.toIntOrNull()

    override fun getLong(key: String): Long? =
        getString(key)?.toLongOrNull()

    override fun getFloat(key: String): Float? =
        getString(key)?.toFloatOrNull()

    override fun getDouble(key: String): Double? =
        getString(key)?.toDoubleOrNull()

    override fun getUUID(key: String): UUID? =
        getString(key)?.let(::UUID)

    override fun setString(key: String, value: String) {
        localStorage.setItem(key, value)
    }

    override fun setBoolean(key: String, value: Boolean) {
        localStorage.setItem(key, value.toString())
    }

    override fun setInt(key: String, value: Int) {
        localStorage.setItem(key, value.toString())
    }

    override fun setLong(key: String, value: Long) {
        localStorage.setItem(key, value.toString())
    }

    override fun setFloat(key: String, value: Float) {
        localStorage.setItem(key, value.toString())
    }

    override fun setDouble(key: String, value: Double) {
        localStorage.setItem(key, value.toString())
    }

    override fun setUUID(key: String, value: UUID) {
        setString(key, value.toString())
    }

    // Secure storage
    // Note: localStorage is not secure. Consider using IndexedDB with encryption for secure storage

    override fun removeSecure(key: String) = remove("secure_$key")
    override fun hasSecureKey(key: String): Boolean = hasKey("secure_$key")
    override fun getSecureString(key: String): String? = getString("secure_$key")
    override fun getSecureBoolean(key: String): Boolean? = getBoolean("secure_$key")
    override fun getSecureInt(key: String): Int? = getInt("secure_$key")
    override fun getSecureLong(key: String): Long? = getLong("secure_$key")
    override fun getSecureFloat(key: String): Float? = getFloat("secure_$key")
    override fun getSecureDouble(key: String): Double? = getDouble("secure_$key")
    override fun getSecureUUID(key: String): UUID? = getUUID("secure_$key")
    override fun setSecureString(key: String, value: String) = setString("secure_$key", value)
    override fun setSecureBoolean(key: String, value: Boolean) = setBoolean("secure_$key", value)
    override fun setSecureInt(key: String, value: Int) = setInt("secure_$key", value)
    override fun setSecureLong(key: String, value: Long) = setLong("secure_$key", value)
    override fun setSecureFloat(key: String, value: Float) = setFloat("secure_$key", value)
    override fun setSecureDouble(key: String, value: Double) = setDouble("secure_$key", value)
    override fun setSecureUUID(key: String, value: UUID) = setUUID("secure_$key", value)

}
