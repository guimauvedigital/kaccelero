package dev.kaccelero.models

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

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
    fun equalsWithSameUUID() {
        val uuid1 = UUID("7e6ae51b-4aab-4f5e-97f5-83ecb6dcf16f")
        val uuid2 = UUID("7e6ae51b-4aab-4f5e-97f5-83ecb6dcf16f")
        assertTrue(uuid1 == uuid2)
        assertEquals(uuid1, uuid2)
    }

    @Test
    fun equalsWithDifferentUUID() {
        val uuid1 = UUID("7e6ae51b-4aab-4f5e-97f5-83ecb6dcf16f")
        val uuid2 = UUID("8f7bf62c-5bbc-4f6f-a8f6-94fdc7edf27f")
        assertFalse(uuid1 == uuid2)
    }

    @Test
    fun equalsWithNull() {
        val uuid = UUID("7e6ae51b-4aab-4f5e-97f5-83ecb6dcf16f")
        assertFalse(uuid.equals(null))
    }

    @Test
    fun equalsWithDifferentType() {
        val uuid = UUID("7e6ae51b-4aab-4f5e-97f5-83ecb6dcf16f")
        assertFalse(uuid.equals("7e6ae51b-4aab-4f5e-97f5-83ecb6dcf16f"))
    }

}
