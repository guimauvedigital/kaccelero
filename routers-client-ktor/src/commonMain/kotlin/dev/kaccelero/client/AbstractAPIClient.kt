package dev.kaccelero.client

import dev.kaccelero.commons.auth.IGetTokenUseCase
import dev.kaccelero.commons.auth.ILogoutUseCase
import dev.kaccelero.commons.auth.IRenewTokenUseCase
import dev.kaccelero.commons.exceptions.APIException
import dev.kaccelero.serializers.Serialization
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

abstract class AbstractAPIClient(
    override val baseUrl: String,
    override val getTokenUseCase: IGetTokenUseCase? = null,
    override val renewTokenUseCase: IRenewTokenUseCase? = null,
    override val logoutUseCase: ILogoutUseCase? = null,
    json: Json? = null,
    engine: HttpClientEngine? = null,
) : IAPIClient {

    private val httpClient = run {
        val block: HttpClientConfig<*>.() -> Unit = {
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
        }
        engine?.let { HttpClient(it, block) } ?: HttpClient(block)
    }

    override suspend fun request(
        method: HttpMethod,
        path: String,
        builder: HttpRequestBuilder.() -> Unit,
    ): HttpResponse {
        val doRequest: suspend () -> HttpResponse = {
            httpClient.request(baseUrl + path) {
                this.method = method
                if (shouldIncludeToken(method, path)) getTokenUseCase?.invoke()?.let { token ->
                    header("Authorization", "Bearer $token")
                }
                builder()
            }
        }
        return try {
            doRequest()
        } catch (exception: APIException) {
            if (exception.code != HttpStatusCode.Unauthorized) throw exception

            val success = try {
                renewTokenUseCase?.invoke(this) ?: false
            } catch (e: Exception) {
                false
            }
            if (success) doRequest() else {
                logoutUseCase?.invoke()
                throw exception
            }
        }
    }

    open fun shouldIncludeToken(method: HttpMethod, path: String): Boolean = true

}
