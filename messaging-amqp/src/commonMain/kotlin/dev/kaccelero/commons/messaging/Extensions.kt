package dev.kaccelero.commons.messaging

import dev.kourier.amqp.AMQPResponse
import dev.kourier.amqp.Field
import io.ktor.callid.*
import io.ktor.http.*
import kotlinx.coroutines.currentCoroutineContext
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

/**
 * Retrieves the current request ID from the coroutine context, if available.
 *
 * @return A map containing the request ID header, or an empty map if not available.
 */
suspend fun mapOfRequestId(): Map<String, Field.LongString> {
    return currentCoroutineContext()[KtorCallIdContextElement]
        ?.let { mapOf(HttpHeaders.XRequestId to Field.LongString(it.callId)) }
        ?: emptyMap()
}

/**
 * Executes a block of code with a specified call ID in the coroutine context, if the call ID is present.
 *
 * @param delivery The AMQP message delivery containing the call ID in headers.
 * @param block The block of code to execute.
 */
suspend fun withCallId(delivery: AMQPResponse.Channel.Message.Delivery, block: suspend () -> Unit) {
    val callId = delivery.message.properties.headers?.get(HttpHeaders.XRequestId)?.let {
        when (it) {
            is Field.LongString -> it.value
            else -> null
        }
    }
    callId?.let { withCallId(it, block) } ?: block()
}
