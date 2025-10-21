package dev.kaccelero.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class UUIDTest {

    @Test
    fun randomUUID() {
        val uuid = UUID()
        assertEquals(36, uuid.toString().length)
        assertEquals(4, uuid.toString().count { it == '-' })
        assertEquals(uuid, uuid)
    }

    @Test
    fun fromString() {
        val uuid = UUID("7e6ae51b-4aab-4f5e-97f5-83ecb6dcf16f")
        assertEquals("7e6ae51b-4aab-4f5e-97f5-83ecb6dcf16f", uuid.toString().lowercase())
    }

    @Test
    fun fromStringWithoutDashes() {
        val uuid = UUID("7E6AE51B4AAB4F5E97F583ECB6DCF16F")
        assertEquals("7e6ae51b-4aab-4f5e-97f5-83ecb6dcf16f", uuid.toString().lowercase())
    }

    @Test
    fun fromStringWithoutDashesLowercase() {
        val uuid = UUID("7e6ae51b4aab4f5e97f583ecb6dcf16f")
        assertEquals("7e6ae51b-4aab-4f5e-97f5-83ecb6dcf16f", uuid.toString().lowercase())
    }

    @Test
    fun fromStringMixedCase() {
        val uuid = UUID("7E6Ae51B-4aAb-4F5e-97f5-83EcB6DcF16F")
        assertEquals("7e6ae51b-4aab-4f5e-97f5-83ecb6dcf16f", uuid.toString().lowercase())
    }

    @Test
    fun equalityWithAndWithoutDashes() {
        val uuid1 = UUID("7e6ae51b-4aab-4f5e-97f5-83ecb6dcf16f")
        val uuid2 = UUID("7E6AE51B4AAB4F5E97F583ECB6DCF16F")
        assertEquals(uuid1, uuid2)
    }

    @Test
    fun invalidUUIDTooShort() {
        assertFailsWith<IllegalArgumentException> {
            UUID("7e6ae51b-4aab-4f5e-97f5")
        }
    }

    @Test
    fun invalidUUIDTooLong() {
        assertFailsWith<IllegalArgumentException> {
            UUID("7e6ae51b-4aab-4f5e-97f5-83ecb6dcf16f-extra")
        }
    }

    @Test
    fun invalidUUIDInvalidCharacters() {
        assertFailsWith<IllegalArgumentException> {
            UUID("7e6ae51b-4aab-4f5e-97f5-83ecb6dcf16g")
        }
    }

}
