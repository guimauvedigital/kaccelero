package dev.kaccelero.client

import dev.kaccelero.commons.auth.IGetTokenUseCase
import dev.kaccelero.commons.auth.ILogoutUseCase
import dev.kaccelero.commons.auth.IRenewTokenUseCase
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * API client interface defining the contract for making HTTP requests.
 */
interface IAPIClient {

    /**
     * Base URL of the API.
     */
    val baseUrl: String

    /**
     * Use case to get the current token, if applicable.
     */
    val getTokenUseCase: IGetTokenUseCase?

    /**
     * Use case to renew the token, if applicable.
     */
    val renewTokenUseCase: IRenewTokenUseCase?

    /**
     * Use case to log out, if applicable.
     */
    val logoutUseCase: ILogoutUseCase?

    /**
     * Makes an HTTP request to the specified path with the given method and request builder.
     * It also applies token management if the relevant use cases are provided.
     *
     * @param method The HTTP method to use (e.g., GET, POST).
     * @param path The API endpoint path.
     * @param builder A lambda to configure the HTTP request.
     *
     * @return The HTTP response.
     */
    suspend fun request(
        method: HttpMethod,
        path: String,
        builder: HttpRequestBuilder.() -> Unit = {},
    ): HttpResponse

}
