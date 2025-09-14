package dev.kaccelero.client

import dev.kaccelero.commons.auth.IGetTokenUseCase
import dev.kaccelero.commons.auth.ILogoutUseCase
import dev.kaccelero.commons.auth.IRenewTokenUseCase
import dev.kaccelero.commons.exceptions.APIException
import dev.kaccelero.serializers.Serialization
import io.ktor.callid.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.serialization.json.Json

/**
 * Base implementation of [IAPIClient] using Ktor HTTP client.
 */
abstract class AbstractAPIClient(
    override val baseUrl: String,
    override val getTokenUseCase: IGetTokenUseCase? = null,
    override val renewTokenUseCase: IRenewTokenUseCase? = null,
    override val logoutUseCase: ILogoutUseCase? = null,
    json: Json? = null,
    engine: HttpClientEngine? = null,
    block: HttpClientConfig<*>.() -> Unit = {},
) : IAPIClient {

    private val httpClient = run {
        val innerBlock: HttpClientConfig<*>.() -> Unit = {
            expectSuccess = true
            HttpResponseValidator {
                handleResponseExceptionWithRequest { exception, _ ->
                    val clientException = exception as? ResponseException
                        ?: return@handleResponseExceptionWithRequest
                    val error = clientException.response.body<Map<String, String>>()["error"]
                        ?: return@handleResponseExceptionWithRequest
                    throw APIException(clientException.response.status, error)
                }
            }
            install(ContentNegotiation) {
                json(json ?: Serialization.json)
            }
            block()
        }
        engine?.let { HttpClient(it, innerBlock) } ?: HttpClient(innerBlock)
    }

    override suspend fun request(
        method: HttpMethod,
        path: String,
        builder: HttpRequestBuilder.() -> Unit,
    ): HttpResponse {
        val doRequest: suspend () -> HttpResponse = {
            httpClient.request(baseUrl + path) {
                this.method = method
                if (shouldIncludeToken(method, path)) getTokenUseCase?.invoke()?.let(::bearerAuth)
                if (shouldPropagateRequestId(method, path)) currentCoroutineContext()[KtorCallIdContextElement]?.let {
                    header(HttpHeaders.XRequestId, it.callId)
                }
                builder()
            }
        }
        return try {
            doRequest()
        } catch (exception: APIException) {
            if (exception.code != HttpStatusCode.Unauthorized) throw exception

            val success = try {
                renewTokenUseCase?.invoke(this) == true
            } catch (e: Exception) {
                false
            }
            if (success) doRequest() else {
                logoutUseCase?.invoke()
                throw exception
            }
        }
    }

    /**
     * Determines whether to include the authentication token in the request.
     * Can be overridden to customize behavior based on method and path.
     * By default, it returns true for all requests.
     *
     * @param method The HTTP method of the request.
     * @param path The path of the request.
     *
     * @return True if the token should be included, false otherwise.
     */
    open fun shouldIncludeToken(method: HttpMethod, path: String): Boolean = true

    /**
     * Determines whether to propagate the request ID in the request.
     * Can be overridden to customize behavior based on method and path.
     * By default, it returns true for all requests.
     *
     * @param method The HTTP method of the request.
     * @param path The path of the request.
     *
     * @return True if the request ID should be propagated, false otherwise.
     */
    open fun shouldPropagateRequestId(method: HttpMethod, path: String): Boolean = true

}
