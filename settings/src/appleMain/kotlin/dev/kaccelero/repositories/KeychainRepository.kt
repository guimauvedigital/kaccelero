package dev.kaccelero.repositories

import dev.kaccelero.extensions.toKString
import kotlinx.cinterop.*
import platform.CoreFoundation.*
import platform.Foundation.CFBridgingRelease
import platform.Foundation.CFBridgingRetain
import platform.Foundation.NSData
import platform.Foundation.NSString
import platform.Security.*
import platform.darwin.OSStatus

@OptIn(UnsafeNumber::class, ExperimentalForeignApi::class)
class KeychainRepository(
    service: String? = null,
    keychainAccessGroup: String? = null,
) {

    private val defaultProperties: Map<CFStringRef?, CFTypeRef?> = mapOf(
        kSecClass to kSecClassGenericPassword
    ) + (service?.let {
        val cfService = CFBridgingRetain(it)
        mapOf(kSecAttrService to cfService)
    } ?: emptyMap()) + (keychainAccessGroup?.let {
        val cfAccessGroup = CFBridgingRetain(it)
        mapOf(kSecAttrAccessGroup to cfAccessGroup)
    } ?: emptyMap())

    fun addKeychainItem(key: String, value: NSData?): Boolean = cfRetain(key, value) { cfKey, cfValue ->
        val status = keyChainOperation(
            kSecAttrAccount to cfKey,
            kSecValueData to cfValue
        ) { SecItemAdd(it, null) }
        status.checkError(errSecDuplicateItem)

        status != errSecDuplicateItem
    }

    fun removeKeychainItem(key: String): Unit = cfRetain(key) { cfKey ->
        val status = keyChainOperation(
            kSecAttrAccount to cfKey,
        ) { SecItemDelete(it) }
        status.checkError(errSecItemNotFound)
    }

    fun updateKeychainItem(key: String, value: NSData?): Unit = cfRetain(key, value) { cfKey, cfValue ->
        val status = keyChainOperation(
            kSecAttrAccount to cfKey,
            kSecReturnData to kCFBooleanFalse
        ) {
            val attributes = cfDictionaryOf(kSecValueData to cfValue)
            val output = SecItemUpdate(it, attributes)
            CFBridgingRelease(attributes)
            output
        }
        status.checkError()
    }

    fun getKeychainItem(key: String): NSData? = cfRetain(key) { cfKey ->
        val cfValue = alloc<CFTypeRefVar>()
        val status = keyChainOperation(
            kSecAttrAccount to cfKey,
            kSecReturnData to kCFBooleanTrue,
            kSecMatchLimit to kSecMatchLimitOne
        ) { SecItemCopyMatching(it, cfValue.ptr) }
        status.checkError(errSecItemNotFound)
        if (status == errSecItemNotFound) {
            return@cfRetain null
        }
        CFBridgingRelease(cfValue.value) as? NSData
    }

    fun hasKeychainItem(key: String): Boolean = cfRetain(key) { cfKey ->
        val status = keyChainOperation(
            kSecAttrAccount to cfKey,
            kSecMatchLimit to kSecMatchLimitOne
        ) { SecItemCopyMatching(it, null) }

        status != errSecItemNotFound
    }

    private inline fun MemScope.keyChainOperation(
        vararg input: Pair<CFStringRef?, CFTypeRef?>,
        operation: (query: CFDictionaryRef?) -> OSStatus,
    ): OSStatus {
        val query = cfDictionaryOf(defaultProperties + mapOf(*input))
        val output = operation(query)
        CFBridgingRelease(query)
        return output
    }

    private fun OSStatus.checkError(vararg expectedErrors: OSStatus) {
        if (this != 0 && this !in expectedErrors) {
            val cfMessage = SecCopyErrorMessageString(this, null)
            val nsMessage = CFBridgingRelease(cfMessage) as? NSString
            val message = nsMessage?.toKString() ?: "Unknown error"
            error("Keychain error $this: $message")
        }
    }

    private fun MemScope.cfDictionaryOf(vararg items: Pair<CFStringRef?, CFTypeRef?>): CFDictionaryRef? =
        cfDictionaryOf(mapOf(*items))

    private fun MemScope.cfDictionaryOf(map: Map<CFStringRef?, CFTypeRef?>): CFDictionaryRef? {
        val size = map.size
        val keys = allocArrayOf(*map.keys.toTypedArray())
        val values = allocArrayOf(*map.values.toTypedArray())
        return CFDictionaryCreate(
            kCFAllocatorDefault,
            keys.reinterpret(),
            values.reinterpret(),
            size.convert(),
            null,
            null
        )
    }

    private inline fun <T> cfRetain(value: Any?, block: MemScope.(CFTypeRef?) -> T): T = memScoped {
        val cfValue = CFBridgingRetain(value)
        return try {
            block(cfValue)
        } finally {
            CFBridgingRelease(cfValue)
        }
    }

    private inline fun <T> cfRetain(value1: Any?, value2: Any?, block: MemScope.(CFTypeRef?, CFTypeRef?) -> T): T =
        memScoped {
            val cfValue1 = CFBridgingRetain(value1)
            val cfValue2 = CFBridgingRetain(value2)
            return try {
                block(cfValue1, cfValue2)
            } finally {
                CFBridgingRelease(cfValue1)
                CFBridgingRelease(cfValue2)
            }
        }

}
