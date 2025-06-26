package dev.kaccelero.plugins

import io.ktor.server.application.*
import io.ktor.server.auth.*

/**
 * Installs API Key authentication mechanism.
 */
fun AuthenticationConfig.apiKey(
    name: String? = null,
    configure: ApiKeyAuthenticationProvider.Configuration.() -> Unit,
) {
    val provider = ApiKeyAuthenticationProvider(ApiKeyAuthenticationProvider.Configuration(name).apply(configure))
    register(provider)
}

/**
 * Alias for function signature that is invoked when verifying header.
 */
typealias ApiKeyAuthenticationFunction = suspend ApplicationCall.(String) -> Principal?

/**
 * Alias for function signature that is called when authentication fails.
 */
typealias ApiKeyAuthChallengeFunction = suspend (ApplicationCall) -> Unit
