package dev.kaccelero.repositories

import dev.kaccelero.models.UUID
import java.util.prefs.Preferences

class NativeSettingsRepository(
    private val preferences: Preferences? = null,
) : INativeSettingsRepository {

    // Default storage

    override fun remove(key: String) {
        preferences?.remove(key)
    }

    override fun hasKey(key: String): Boolean =
        preferences?.get(key, null) != null

    override fun getString(key: String): String? =
        if (hasKey(key)) preferences?.get(key, null) else null

    override fun getBoolean(key: String): Boolean? =
        if (hasKey(key)) preferences?.getBoolean(key, false) else null

    override fun getInt(key: String): Int? =
        if (hasKey(key)) preferences?.getInt(key, 0) else null

    override fun getLong(key: String): Long? =
        if (hasKey(key)) preferences?.getLong(key, 0) else null

    override fun getFloat(key: String): Float? =
        if (hasKey(key)) preferences?.getFloat(key, 0f) else null

    override fun getDouble(key: String): Double? =
        if (hasKey(key)) preferences?.getDouble(key, 0.0) else null

    override fun getUUID(key: String): UUID? =
        if (hasKey(key)) getString(key)?.let(::UUID) else null

    override fun setString(key: String, value: String) {
        preferences?.put(key, value)
    }

    override fun setBoolean(key: String, value: Boolean) {
        preferences?.putBoolean(key, value)
    }

    override fun setInt(key: String, value: Int) {
        preferences?.putInt(key, value)
    }

    override fun setLong(key: String, value: Long) {
        preferences?.putLong(key, value)
    }

    override fun setFloat(key: String, value: Float) {
        preferences?.putFloat(key, value)
    }

    override fun setDouble(key: String, value: Double) {
        preferences?.putDouble(key, value)
    }

    override fun setUUID(key: String, value: UUID) {
        setString(key, value.toString())
    }

    // Secure storage
    // TODO: Use system keyring for secure storage

    override fun removeSecure(key: String) = remove(key)
    override fun hasSecureKey(key: String): Boolean = hasKey(key)
    override fun getSecureString(key: String): String? = getString(key)
    override fun getSecureBoolean(key: String): Boolean? = getBoolean(key)
    override fun getSecureInt(key: String): Int? = getInt(key)
    override fun getSecureLong(key: String): Long? = getLong(key)
    override fun getSecureFloat(key: String): Float? = getFloat(key)
    override fun getSecureDouble(key: String): Double? = getDouble(key)
    override fun getSecureUUID(key: String): UUID? = getUUID(key)
    override fun setSecureString(key: String, value: String) = setString(key, value)
    override fun setSecureBoolean(key: String, value: Boolean) = setBoolean(key, value)
    override fun setSecureInt(key: String, value: Int) = setInt(key, value)
    override fun setSecureLong(key: String, value: Long) = setLong(key, value)
    override fun setSecureFloat(key: String, value: Float) = setFloat(key, value)
    override fun setSecureDouble(key: String, value: Double) = setDouble(key, value)
    override fun setSecureUUID(key: String, value: UUID) = setUUID(key, value)

}
