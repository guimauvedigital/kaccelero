package dev.kaccelero.repositories

import dev.kaccelero.models.UUID
import platform.Foundation.NSUserDefaults
import kotlin.test.*

class NativeSettingsRepositoryTest {

    private lateinit var userDefaults: NSUserDefaults
    private lateinit var keychainRepository: KeychainRepository
    private lateinit var repository: NativeSettingsRepository

    @BeforeTest
    fun setup() {
        userDefaults = NSUserDefaults.standardUserDefaults
        keychainRepository = KeychainRepository()
        repository = NativeSettingsRepository(
            userDefaults = userDefaults,
            keychainRepository = keychainRepository,
            keychainUsingNSKeyedArchiver = true
        )

        // Clear any existing test data
        listOf(
            "test_string", "test_boolean", "test_int", "test_long",
            "test_float", "test_double", "test_uuid", "key1", "key2", "key3"
        ).forEach { userDefaults.removeObjectForKey(it) }
    }

    // String tests
    @Test
    fun testStringStorageAndRetrieval() {
        val key = "test_string"
        val value = "Hello, World!"

        assertFalse(repository.hasKey(key))
        assertNull(repository.getString(key))

        repository.setString(key, value)
        assertTrue(repository.hasKey(key))
        assertEquals(value, repository.getString(key))

        repository.remove(key)
        assertFalse(repository.hasKey(key))
        assertNull(repository.getString(key))
    }

    // Boolean tests
    @Test
    fun testBooleanStorageAndRetrieval() {
        val key = "test_boolean"
        val value = true

        assertNull(repository.getBoolean(key))

        repository.setBoolean(key, value)
        assertEquals(value, repository.getBoolean(key))

        repository.setBoolean(key, false)
        assertEquals(false, repository.getBoolean(key))

        repository.remove(key)
        assertNull(repository.getBoolean(key))
    }

    // Int tests
    @Test
    fun testIntStorageAndRetrieval() {
        val key = "test_int"
        val value = 42

        assertNull(repository.getInt(key))

        repository.setInt(key, value)
        assertEquals(value, repository.getInt(key))

        repository.setInt(key, -100)
        assertEquals(-100, repository.getInt(key))

        repository.remove(key)
        assertNull(repository.getInt(key))
    }

    // Long tests
    @Test
    fun testLongStorageAndRetrieval() {
        val key = "test_long"
        val value = 9223372036854775807L

        assertNull(repository.getLong(key))

        repository.setLong(key, value)
        assertEquals(value, repository.getLong(key))

        repository.remove(key)
        assertNull(repository.getLong(key))
    }

    // Float tests
    @Test
    fun testFloatStorageAndRetrieval() {
        val key = "test_float"
        val value = 3.14f

        assertNull(repository.getFloat(key))

        repository.setFloat(key, value)
        assertEquals(value, repository.getFloat(key))

        repository.remove(key)
        assertNull(repository.getFloat(key))
    }

    // Double tests
    @Test
    fun testDoubleStorageAndRetrieval() {
        val key = "test_double"
        val value = 3.141592653589793

        assertNull(repository.getDouble(key))

        repository.setDouble(key, value)
        assertEquals(value, repository.getDouble(key))

        repository.remove(key)
        assertNull(repository.getDouble(key))
    }

    // UUID tests
    @Test
    fun testUUIDStorageAndRetrieval() {
        val key = "test_uuid"
        val value = UUID()

        assertNull(repository.getUUID(key))

        repository.setUUID(key, value)
        assertEquals(value.toString(), repository.getUUID(key)?.toString())

        repository.remove(key)
        assertNull(repository.getUUID(key))
    }

    // Secure storage tests
    @Test
    fun testSecureStringStorageAndRetrieval() {
        val key = "test_secure_string"
        val value = "SecureData123"

        assertFalse(repository.hasSecureKey(key))
        assertNull(repository.getSecureString(key))

        repository.setSecureString(key, value)
        assertTrue(repository.hasSecureKey(key))
        assertEquals(value, repository.getSecureString(key))

        repository.removeSecure(key)
        assertFalse(repository.hasSecureKey(key))
        assertNull(repository.getSecureString(key))
    }

    @Test
    fun testSecureBooleanStorageAndRetrieval() {
        val key = "test_secure_boolean"
        val value = true

        repository.setSecureBoolean(key, value)
        assertEquals(value, repository.getSecureBoolean(key))

        repository.removeSecure(key)
        assertNull(repository.getSecureBoolean(key))
    }

    @Test
    fun testSecureIntStorageAndRetrieval() {
        val key = "test_secure_int"
        val value = 999

        repository.setSecureInt(key, value)
        assertEquals(value, repository.getSecureInt(key))

        repository.removeSecure(key)
        assertNull(repository.getSecureInt(key))
    }

    @Test
    fun testSecureLongStorageAndRetrieval() {
        val key = "test_secure_long"
        val value = 123456789L

        repository.setSecureLong(key, value)
        assertEquals(value, repository.getSecureLong(key))

        repository.removeSecure(key)
        assertNull(repository.getSecureLong(key))
    }

    @Test
    fun testSecureFloatStorageAndRetrieval() {
        val key = "test_secure_float"
        val value = 2.718f

        repository.setSecureFloat(key, value)
        assertEquals(value, repository.getSecureFloat(key))

        repository.removeSecure(key)
        assertNull(repository.getSecureFloat(key))
    }

    @Test
    fun testSecureDoubleStorageAndRetrieval() {
        val key = "test_secure_double"
        val value = 1.618033988749895

        repository.setSecureDouble(key, value)
        assertEquals(value, repository.getSecureDouble(key))

        repository.removeSecure(key)
        assertNull(repository.getSecureDouble(key))
    }

    @Test
    fun testSecureUUIDStorageAndRetrieval() {
        val key = "test_secure_uuid"
        val value = UUID()

        repository.setSecureUUID(key, value)
        assertEquals(value.toString(), repository.getSecureUUID(key)?.toString())

        repository.removeSecure(key)
        assertNull(repository.getSecureUUID(key))
    }

    // Edge cases
    @Test
    fun testNullPreferencesBehavior() {
        val nullRepository = NativeSettingsRepository(null, null)

        nullRepository.setString("key", "value")
        assertNull(nullRepository.getString("key"))
        assertFalse(nullRepository.hasKey("key"))
    }

    @Test
    fun testMultipleKeysStorage() {
        repository.setString("key1", "value1")
        repository.setString("key2", "value2")
        repository.setInt("key3", 123)

        assertEquals("value1", repository.getString("key1"))
        assertEquals("value2", repository.getString("key2"))
        assertEquals(123, repository.getInt("key3"))

        repository.remove("key2")
        assertNull(repository.getString("key2"))
        assertEquals("value1", repository.getString("key1"))
        assertEquals(123, repository.getInt("key3"))
    }

}
