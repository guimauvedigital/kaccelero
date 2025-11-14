package dev.kaccelero.plugins

import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class HealthTest {

    private fun installApp(
        application: ApplicationTestBuilder,
        configure: HealthConfiguration.() -> Unit = {},
    ): HttpClient {
        application.application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json(Json)
            }
            install(Health, configure)
        }
        return application.createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json)
            }
        }
    }

    @Test
    fun testHealthzNoCheck() = testApplication {
        val client = installApp(this)
        val response = client.get("/healthz")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(emptyMap<String, Boolean>(), response.body())
    }

    @Test
    fun testReadyzNoCheck() = testApplication {
        val client = installApp(this)
        val response = client.get("/readyz")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(emptyMap<String, Boolean>(), response.body())
    }

    @Test
    fun testHealthzDisabled() = testApplication {
        val client = installApp(this) {
            disableHealthCheck()
        }
        val response = client.get("/healthz")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testReadyzDisabled() = testApplication {
        val client = installApp(this) {
            disableReadyCheck()
        }
        val response = client.get("/readyz")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

    @Test
    fun testHealthzCheckSuccess() = testApplication {
        val client = installApp(this) {
            healthCheck("test") { true }
        }
        val response = client.get("/healthz")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(mapOf("test" to true), response.body())
    }

    @Test
    fun testHealthzCheckFailure() = testApplication {
        val client = installApp(this) {
            healthCheck("test") { false }
        }
        val response = client.get("/healthz")
        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        assertEquals(mapOf("test" to false), response.body())
    }

    @Test
    fun testReadyzCheckSuccess() = testApplication {
        val client = installApp(this) {
            readyCheck("test") { true }
        }
        val response = client.get("/readyz")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(mapOf("test" to true), response.body())
    }

    @Test
    fun testReadyzCheckFailure() = testApplication {
        val client = installApp(this) {
            readyCheck("test") { false }
        }
        val response = client.get("/readyz")
        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        assertEquals(mapOf("test" to false), response.body())
    }

    @Test
    fun testCustomSuccess() = testApplication {
        val client = installApp(this) {
            customCheck("/smoketest", "test") { true }
        }
        val response = client.get("/smoketest")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(mapOf("test" to true), response.body())
    }

    @Test
    fun testCustomFailure() = testApplication {
        val client = installApp(this) {
            customCheck("/smoketest", "test") { false }
        }
        val response = client.get("/smoketest")
        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        assertEquals(mapOf("test" to false), response.body())
    }

    @Test
    fun testCachingReturnsServiceUnavailableUntilFirstCheck() = testApplication {
        var checkExecuted = false
        val client = installApp(this) {
            enableCachingResults(
                refreshInterval = kotlin.time.Duration.parse("1s"),
                checkTimeout = kotlin.time.Duration.parse("500ms"),
            )
            healthCheck("test") {
                kotlinx.coroutines.delay(200)
                checkExecuted = true
                true
            }
        }

        // First request should return ServiceUnavailable with empty map (cache not initialized)
        val response1 = client.get("/healthz")
        assertEquals(HttpStatusCode.ServiceUnavailable, response1.status)
        assertEquals(emptyMap<String, Boolean>(), response1.body())

        // Wait for initial check to complete
        kotlinx.coroutines.delay(300)

        // Second request should return cached result
        val response2 = client.get("/healthz")
        assertEquals(HttpStatusCode.OK, response2.status)
        assertEquals(mapOf("test" to true), response2.body())
        assert(checkExecuted) { "Check should have been executed" }
    }

    @Test
    fun testCachingEnabledReturnsServiceUnavailableInitially() = testApplication {
        val client = installApp(this) {
            enableCachingResults(
                refreshInterval = kotlin.time.Duration.parse("1s"),
                checkTimeout = kotlin.time.Duration.parse("500ms"),
            )
            healthCheck("test") {
                // Slow check to ensure we can make request before cache is populated
                kotlinx.coroutines.delay(200)
                true
            }
        }

        // Make request immediately before cache is populated
        val response = client.get("/healthz")
        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        assertEquals(emptyMap<String, Boolean>(), response.body())
    }

    @Test
    fun testCachingReturnsResultsAfterInitialCheck() = testApplication {
        val client = installApp(this) {
            enableCachingResults(
                refreshInterval = kotlin.time.Duration.parse("10s"),
                checkTimeout = kotlin.time.Duration.parse("2s"),
            )
            healthCheck("test") { true }
        }

        // Wait for initial check to complete
        kotlinx.coroutines.delay(500)

        // Should now return cached result
        val response = client.get("/healthz")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(mapOf("test" to true), response.body())
    }

    @Test
    fun testCachingWithFailedCheck() = testApplication {
        val client = installApp(this) {
            enableCachingResults(
                refreshInterval = kotlin.time.Duration.parse("10s"),
                checkTimeout = kotlin.time.Duration.parse("2s"),
            )
            healthCheck("test") { false }
        }

        // Wait for initial check
        kotlinx.coroutines.delay(500)

        // Should cache the failure
        val response = client.get("/healthz")
        assertEquals(HttpStatusCode.ServiceUnavailable, response.status)
        assertEquals(mapOf("test" to false), response.body())
    }

    @Test
    fun testCachingStalenessDetection() = testApplication {
        val client = installApp(this) {
            enableCachingResults(
                refreshInterval = kotlin.time.Duration.parse("10s"),
                checkTimeout = kotlin.time.Duration.parse("200ms"),
                stalenessThreshold = kotlin.time.Duration.parse("300ms"),
            )
            healthCheck("test") { true }
        }

        // Wait for initial check
        kotlinx.coroutines.delay(100)

        // First request should return OK
        val response1 = client.get("/healthz")
        assertEquals(HttpStatusCode.OK, response1.status)

        // Wait for cache to become stale
        kotlinx.coroutines.delay(400)

        // Should return ServiceUnavailable due to stale cache
        val response2 = client.get("/healthz")
        assertEquals(HttpStatusCode.ServiceUnavailable, response2.status)
        assertEquals(mapOf("test" to true), response2.body())
    }

}
