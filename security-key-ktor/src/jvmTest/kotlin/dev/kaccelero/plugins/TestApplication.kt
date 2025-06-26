package dev.kaccelero.plugins

import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

const val apiKeyAuth = "api-key"

object Routes {
    const val AUTHENTICATED = "authenticated"
    const val OPEN = "open"
}

fun buildApplicationModule(
    apiKeyConfig: ApiKeyAuthenticationProvider.Configuration.() -> Unit,
): ApplicationModule = {
    install(ContentNegotiation) {
        json()
    }

    install(Authentication) {
        apiKey(apiKeyAuth, apiKeyConfig)
    }

    routing {
        authenticate(apiKeyAuth) {
            route(Routes.AUTHENTICATED) {
                get { respondPrincipal() }
                post { respondPrincipal() }
            }
        }
        route(Routes.OPEN) {
            get { respondOk() }
            post { respondOk() }
        }
    }
}

typealias ApplicationModule = Application.() -> Unit

suspend fun RoutingContext.respondOk() {
    call.respond(HttpStatusCode.OK)
}

suspend fun RoutingContext.respondPrincipal() {
    val principal = call.principal<Principal>() ?: throw IllegalArgumentException("No Principal found!")
    call.respond(principal)
}
