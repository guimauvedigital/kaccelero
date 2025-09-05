package dev.kaccelero.commons.messaging

import kotlinx.coroutines.delay

/**
 * Attempts to execute a block of code with retries and a delay between attempts.
 *
 * @param T The type of the result returned by the block.
 * @param attempts The maximum number of attempts to execute the block before giving up. Default is 3.
 * @param delay The delay in milliseconds between attempts. Default is 5000ms (5 seconds).
 * @param block The block of code to execute.
 *
 * @return The result of the block if successful.
 * @throws Exception If the block fails after the maximum number of attempts.
 */
suspend inline fun <reified T> tryWithAttempts(
    attempts: Int = 3,
    delay: Long = 5000,
    block: () -> T,
): T {
    var leftAttempts = attempts
    while (true) {
        try {
            return block()
        } catch (e: Exception) {
            leftAttempts--
            if (leftAttempts <= 0) throw e
            delay(delay) // Try again after delay
        }
    }
}
