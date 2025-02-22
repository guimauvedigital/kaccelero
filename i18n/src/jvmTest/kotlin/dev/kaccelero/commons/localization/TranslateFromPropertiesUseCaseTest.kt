package dev.kaccelero.commons.localization

import java.util.*
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class TranslateFromPropertiesUseCaseTest {

    @Test
    fun testInvoke() {
        val useCase = TranslateFromPropertiesUseCase()
        assertEquals("Hello world!", useCase(Locale.ENGLISH, "hello_world"))
    }

    @Test
    fun testInvokeFrench() {
        val useCase = TranslateFromPropertiesUseCase()
        assertEquals("Coucou tout le monde !", useCase(Locale.FRENCH, "hello_world"))
    }

    @Test
    fun testInvokeArgs() {
        val useCase = TranslateFromPropertiesUseCase()
        assertEquals("Hello Nathan!", useCase(Locale.ENGLISH, "hello_arg", listOf("Nathan")))
    }

    @Test
    fun testInvokeMissing() {
        val useCase = TranslateFromPropertiesUseCase()
        val exception = assertFailsWith<MissingResourceException> {
            useCase(Locale.ENGLISH, "missing")
        }
        assertEquals("missing", exception.key)
    }

    @Test
    fun testInvokeMissingSilent() {
        val useCase = TranslateFromPropertiesUseCase(silentMissingResourceException = true)
        assertEquals("missing", useCase(Locale.ENGLISH, "missing"))
    }

}
