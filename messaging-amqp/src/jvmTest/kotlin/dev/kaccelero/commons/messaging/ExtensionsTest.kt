package dev.kaccelero.commons.messaging

import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class ExtensionsTest {

    @Test
    fun tryWithReloadingReturnsResultOnFirstTry() = runBlocking {
        val result = tryWithAttempts { "success" }
        assertEquals("success", result)
    }

    @Test
    fun tryWithReloadingRetriesAndSucceeds() = runBlocking {
        var count = 0
        val result = tryWithAttempts(3, 0) {
            count++
            if (count < 2) throw RuntimeException("fail")
            "done"
        }
        assertEquals("done", result)
        assertEquals(2, count)
    }

    @Test
    fun tryWithReloadingThrowsAfterMaxTries() = runBlocking {
        var count = 0
        assertFailsWith<RuntimeException> {
            tryWithAttempts(3, 0) {
                count++
                throw RuntimeException("fail")
            }
        }
        assertEquals(3, count)
    }

    @Test
    fun tryWithReloadingEnsureCompilesWithSuspendParameters() = runBlocking {
        tryWithAttempts(
            block = { delay(1) },
        )
    }

}
