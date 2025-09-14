package dev.kaccelero.client

import dev.kaccelero.models.UUID
import io.ktor.callid.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class AbstractAPIClientTest {

    @Test
    fun testRequestContainsNoRequestIdIfNotSpecified() = runBlocking {
        val client = object : AbstractAPIClient(
            "https://example.com",
            engine = MockEngine { request ->
                assertEquals(null, request.headers[HttpHeaders.XRequestId])
                respond("OK", HttpStatusCode.OK)
            }
        ) {}
        client.request(HttpMethod.Get, "/test")
        Unit
    }

    @Test
    fun testRequestContainsNoRequestIdIfNotEnabled() = runBlocking {
        val client = object : AbstractAPIClient(
            "https://example.com",
            engine = MockEngine { request ->
                assertEquals(null, request.headers[HttpHeaders.XRequestId])
                respond("OK", HttpStatusCode.OK)
            }
        ) {
            override fun shouldPropagateRequestId(method: HttpMethod, path: String): Boolean = false
        }
        withCallId(UUID().toString()) {
            client.request(HttpMethod.Get, "/test")
        }
    }

    @Test
    fun testRequestContainsOriginalRequestId() = runBlocking {
        val requestId = UUID().toString()
        val client = object : AbstractAPIClient(
            "https://example.com",
            engine = MockEngine { request ->
                assertEquals(requestId, request.headers[HttpHeaders.XRequestId])
                respond("OK", HttpStatusCode.OK)
            }
        ) {}
        withCallId(requestId) {
            client.request(HttpMethod.Get, "/test")
        }
    }

}
