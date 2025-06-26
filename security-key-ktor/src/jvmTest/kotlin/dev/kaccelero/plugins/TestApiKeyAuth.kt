package dev.kaccelero.plugins

import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.testing.*
import kotlinx.serialization.Serializable
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals

class TestApiKeyAuth {

    @Serializable
    private data class ApiKeyPrincipal(val key: String) : Principal

    private val defaultHeader = "X-Api-Key"

    @Test
    fun `test apikey auth does not influence open routes`() {
        val apiKey = UUID.randomUUID().toString()

        val module = buildApplicationModule {
            validate { header -> header.takeIf { it == apiKey }?.let { ApiKeyPrincipal(it) } }
        }

        testApplication {
            application(module)
            val client = createClient { install(ContentNegotiation) { json() } }

            var response = client.get(Routes.OPEN)
            assertEquals(HttpStatusCode.OK, response.status)

            response = client.get(Routes.OPEN) {
                header(defaultHeader, apiKey)
            }
            assertEquals(HttpStatusCode.OK, response.status)

            response = client.get(Routes.OPEN) {
                header(defaultHeader, "${apiKey}-wrong")
            }
            assertEquals(HttpStatusCode.OK, response.status)
        }
    }

    @Test
    fun `test reasonable defaults work`() {
        val apiKey = UUID.randomUUID().toString()

        val module = buildApplicationModule {
            validate { header -> header.takeIf { it == apiKey }?.let { ApiKeyPrincipal(it) } }
        }

        testApplication {
            application(module)
            val client = createClient { install(ContentNegotiation) { json() } }

            // correct header
            val response = client.get(Routes.AUTHENTICATED) {
                header(defaultHeader, apiKey)
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val principal = response.body<ApiKeyPrincipal>()
            assertEquals(principal, ApiKeyPrincipal(apiKey))

            // incorrect header
            val unauthorizedResponse = client.get(Routes.AUTHENTICATED) {
                header(defaultHeader, "${apiKey}-wrong")
            }
            assertEquals(HttpStatusCode.Unauthorized, unauthorizedResponse.status)
        }
    }


    @Test
    fun `test auth should accept valid api key`() {
        // use different from default code to verify that it actually works
        val errorStatus = HttpStatusCode.Conflict
        val header = "hello"
        val apiKey = "world"

        val module = buildApplicationModule {
            headerName = header
            challenge { call -> call.respond(errorStatus) }
            validate { header -> header.takeIf { it == apiKey }?.let { ApiKeyPrincipal(it) } }
        }
        testApplication {
            application(module)
            val client = createClient { install(ContentNegotiation) { json() } }

            val response = client.get(Routes.AUTHENTICATED) {
                header(header, apiKey)
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val principal = response.body<ApiKeyPrincipal>()
            assertEquals(principal, ApiKeyPrincipal(apiKey))
        }
    }

    @Test
    fun `test auth should accept reject invalid api key`() {
        val errorStatus = HttpStatusCode.Conflict
        val header = "hello"
        val apiKey = "world"

        val module = buildApplicationModule {
            headerName = header
            challenge { call -> call.respond(errorStatus) }
            validate { header -> header.takeIf { it == apiKey }?.let { ApiKeyPrincipal(it) } }
        }
        testApplication {
            application(module)
            val client = createClient { install(ContentNegotiation) { json() } }

            // correct header
            val response = client.get(Routes.AUTHENTICATED) {
                header(header, apiKey)
            }
            assertEquals(HttpStatusCode.OK, response.status)
            val principal = response.body<ApiKeyPrincipal>()
            assertEquals(principal, ApiKeyPrincipal(apiKey))

            // incorrect header
            val unauthorizedResponse = client.get(Routes.AUTHENTICATED) {
                header(header, "${apiKey}-wrong")
            }
            assertEquals(errorStatus, unauthorizedResponse.status)
        }
    }

}
