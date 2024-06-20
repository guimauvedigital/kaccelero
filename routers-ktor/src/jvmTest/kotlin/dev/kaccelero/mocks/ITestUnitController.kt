package dev.kaccelero.mocks

import dev.kaccelero.annotations.*
import dev.kaccelero.commons.responses.BytesResponse
import dev.kaccelero.commons.responses.StatusResponse
import dev.kaccelero.commons.responses.StreamResponse
import dev.kaccelero.controllers.IUnitController
import io.ktor.server.websocket.*

interface ITestUnitController : IUnitController {

    @APIMapping
    @TemplateMapping("hello.ftl")
    @Path("GET", "/hello")
    suspend fun hello(): String

    @APIMapping
    @TemplateMapping("hello.ftl")
    @Path("GET", "/hello/query")
    suspend fun helloQuery(@QueryParameter name: String): String

    @APIMapping
    @TemplateMapping("hello.ftl")
    @Path("GET", "/hello/path/{name}")
    suspend fun helloPath(@PathParameter name: String): String

    @APIMapping
    @TemplateMapping("hello.ftl")
    @Path("POST", "/hello")
    suspend fun postHello(@Payload payload: TestCreatePayload): String

    @WebSocketMapping
    @Path("GET", "/hello")
    suspend fun hello(session: DefaultWebSocketServerSession)

    @APIMapping
    @TemplateMapping("hello.ftl")
    @Path("GET", "/status")
    suspend fun status(): StatusResponse<String>

    @APIMapping
    @TemplateMapping("hello.ftl")
    @Path("GET", "/bytes")
    suspend fun bytes(): BytesResponse

    @APIMapping
    @TemplateMapping("hello.ftl")
    @Path("GET", "/stream")
    suspend fun stream(): StreamResponse

    @APIMapping
    @AdminTemplateMapping("dashboard.ftl")
    @Path("GET", "/")
    suspend fun dashboard()

}
