package dev.kaccelero.repositories

import dev.kaccelero.extensions.*
import dev.kaccelero.models.UUID
import kotlinx.cinterop.BetaInteropApi
import platform.Foundation.NSNumber
import platform.Foundation.NSUserDefaults

class NativeSettingsRepository(
    private val userDefaults: NSUserDefaults? = null,
    private val keychainRepository: KeychainRepository? = null,
    private val keychainUsingNSKeyedArchiver: Boolean = true,
) : INativeSettingsRepository {

    // Default storage

    override fun remove(key: String) {
        userDefaults?.removeObjectForKey(key)
    }

    override fun hasKey(key: String): Boolean =
        userDefaults?.objectForKey(key) != null

    override fun getString(key: String): String? =
        if (hasKey(key)) userDefaults?.stringForKey(key) else null

    override fun getBoolean(key: String): Boolean? =
        if (hasKey(key)) userDefaults?.boolForKey(key) else null

    override fun getInt(key: String): Int? =
        if (hasKey(key)) userDefaults?.integerForKey(key)?.toInt() else null

    override fun getLong(key: String): Long? =
        if (hasKey(key)) userDefaults?.integerForKey(key) else null

    override fun getFloat(key: String): Float? =
        if (hasKey(key)) userDefaults?.doubleForKey(key)?.toFloat() else null

    override fun getDouble(key: String): Double? =
        if (hasKey(key)) userDefaults?.doubleForKey(key) else null

    override fun getUUID(key: String): UUID? =
        if (hasKey(key)) getString(key)?.let(::UUID) else null

    override fun setString(key: String, value: String) {
        userDefaults?.setObject(value, key)
    }

    override fun setBoolean(key: String, value: Boolean) {
        userDefaults?.setBool(value, key)
    }

    override fun setInt(key: String, value: Int) {
        userDefaults?.setInteger(value.toLong(), key)
    }

    override fun setLong(key: String, value: Long) {
        userDefaults?.setInteger(value, key)
    }

    override fun setFloat(key: String, value: Float) {
        userDefaults?.setDouble(value.toDouble(), key)
    }

    override fun setDouble(key: String, value: Double) {
        userDefaults?.setDouble(value, key)
    }

    override fun setUUID(key: String, value: UUID) {
        userDefaults?.setObject(value.toString(), key)
    }

    // Secure storage

    override fun removeSecure(key: String) {
        keychainRepository?.removeKeychainItem(key)
    }

    override fun hasSecureKey(key: String): Boolean =
        keychainRepository?.hasKeychainItem(key) == true

    @OptIn(BetaInteropApi::class)
    override fun getSecureString(key: String): String? =
        keychainRepository?.getKeychainItem(key)?.toNSString(keychainUsingNSKeyedArchiver)?.toKString()

    override fun getSecureBoolean(key: String): Boolean? =
        keychainRepository?.getKeychainItem(key)?.toNSNumber(keychainUsingNSKeyedArchiver)?.boolValue

    override fun getSecureInt(key: String): Int? =
        keychainRepository?.getKeychainItem(key)?.toNSNumber(keychainUsingNSKeyedArchiver)?.intValue

    override fun getSecureLong(key: String): Long? =
        keychainRepository?.getKeychainItem(key)?.toNSNumber(keychainUsingNSKeyedArchiver)?.longValue

    override fun getSecureFloat(key: String): Float? =
        keychainRepository?.getKeychainItem(key)?.toNSNumber(keychainUsingNSKeyedArchiver)?.floatValue

    override fun getSecureDouble(key: String): Double? =
        keychainRepository?.getKeychainItem(key)?.toNSNumber(keychainUsingNSKeyedArchiver)?.doubleValue

    override fun getSecureUUID(key: String): UUID? =
        keychainRepository?.getKeychainItem(key)?.toNSUUID(keychainUsingNSKeyedArchiver)?.let(::UUID)

    override fun setSecureString(key: String, value: String) {
        keychainRepository?.setKeychainItem(key, value.toNSString().toNSData(keychainUsingNSKeyedArchiver) ?: return)
    }

    override fun setSecureBoolean(key: String, value: Boolean) {
        keychainRepository?.setKeychainItem(key, NSNumber(value).toNSData(keychainUsingNSKeyedArchiver) ?: return)
    }

    override fun setSecureInt(key: String, value: Int) {
        keychainRepository?.setKeychainItem(key, NSNumber(value).toNSData(keychainUsingNSKeyedArchiver) ?: return)
    }

    override fun setSecureLong(key: String, value: Long) {
        keychainRepository?.setKeychainItem(
            key, NSNumber(long = value).toNSData(keychainUsingNSKeyedArchiver) ?: return
        )
    }

    override fun setSecureFloat(key: String, value: Float) {
        keychainRepository?.setKeychainItem(key, NSNumber(value).toNSData(keychainUsingNSKeyedArchiver) ?: return)
    }

    override fun setSecureDouble(key: String, value: Double) {
        keychainRepository?.setKeychainItem(key, NSNumber(value).toNSData(keychainUsingNSKeyedArchiver) ?: return)
    }

    override fun setSecureUUID(key: String, value: UUID) {
        keychainRepository?.setKeychainItem(key, value.nsUUID.toNSData(keychainUsingNSKeyedArchiver) ?: return)
    }

}
