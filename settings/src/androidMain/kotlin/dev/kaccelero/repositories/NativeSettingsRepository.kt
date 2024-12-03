package dev.kaccelero.repositories

import android.content.SharedPreferences
import dev.kaccelero.models.UUID

class NativeSettingsRepository(
    private val sharedPreferences: SharedPreferences? = null,
) : INativeSettingsRepository {

    // Default storage

    override fun remove(key: String) {
        sharedPreferences?.edit()?.remove(key)?.apply()
    }

    override fun hasKey(key: String): Boolean =
        sharedPreferences?.contains(key) == true

    override fun getString(key: String): String? =
        if (hasKey(key)) sharedPreferences?.getString(key, null)?.takeIf { it != "null" } else null

    override fun getBoolean(key: String): Boolean? =
        if (hasKey(key)) sharedPreferences?.getBoolean(key, false) else null

    override fun getInt(key: String): Int? =
        if (hasKey(key)) sharedPreferences?.getInt(key, 0) else null

    override fun getLong(key: String): Long? =
        if (hasKey(key)) sharedPreferences?.getLong(key, 0) else null

    override fun getFloat(key: String): Float? =
        if (hasKey(key)) sharedPreferences?.getFloat(key, 0f) else null

    override fun getDouble(key: String): Double? =
        if (hasKey(key)) sharedPreferences?.getString(key, null)?.toDoubleOrNull() else null

    override fun getUUID(key: String): UUID? =
        if (hasKey(key)) getString(key)?.let(::UUID) else null

    override fun setString(key: String, value: String) {
        sharedPreferences?.edit()?.putString(key, value)?.apply()
    }

    override fun setBoolean(key: String, value: Boolean) {
        sharedPreferences?.edit()?.putBoolean(key, value)?.apply()
    }

    override fun setInt(key: String, value: Int) {
        sharedPreferences?.edit()?.putInt(key, value)?.apply()
    }

    override fun setLong(key: String, value: Long) {
        sharedPreferences?.edit()?.putLong(key, value)?.apply()
    }

    override fun setFloat(key: String, value: Float) {
        sharedPreferences?.edit()?.putFloat(key, value)?.apply()
    }

    override fun setDouble(key: String, value: Double) {
        sharedPreferences?.edit()?.putString(key, value.toString())?.apply()
    }

    override fun setUUID(key: String, value: UUID) {
        setString(key, value.toString())
    }

    // Secure storage
    // TODO: Use Android Keystore for secure storage

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
