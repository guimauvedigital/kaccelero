package dev.kaccelero.routers

import dev.kaccelero.commons.responses.BytesResponse
import dev.kaccelero.commons.responses.StatusResponse
import dev.kaccelero.commons.responses.StreamResponse
import dev.kaccelero.mocks.ITestUnitController
import dev.kaccelero.models.UUID
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.mockk.coEvery
import io.mockk.mockk
import io.swagger.v3.oas.models.OpenAPI
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class APIUnitRouterTest {

    private fun installApp(application: ApplicationTestBuilder): HttpClient {
        application.application {
            install(io.ktor.server.plugins.contentnegotiation.ContentNegotiation) {
                json(Json)
            }
        }
        return application.createClient {
            install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
                json(Json)
            }
        }
    }

    @Test
    fun testAPIBasicRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestUnitController>()
        val router = APIUnitRouter(
            controller,
            ITestUnitController::class
        )
        coEvery { controller.hello() } returns "Hello world"
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/hello")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello world", response.body())
    }

    @Test
    fun testAPIQueryParameterRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestUnitController>()
        val router = APIUnitRouter(
            controller,
            ITestUnitController::class
        )
        coEvery { controller.helloQuery("world") } returns "Hello world"
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/hello/query?name=world")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello world", response.body())
    }

    @Test
    fun testAPIQueryParameterOptionalRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestUnitController>()
        val router = APIUnitRouter(
            controller,
            ITestUnitController::class
        )
        coEvery { controller.helloQueryOptional(null) } returns "Hello world"
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/hello/optional")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello world", response.body())
    }

    @Test
    fun testAPIQueryParameterMissingRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestUnitController>()
        val router = APIUnitRouter(
            controller,
            ITestUnitController::class
        )
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/hello/query")
        assertEquals(HttpStatusCode.BadRequest, response.status)
    }

    @Test
    fun testAPIQueryParameterRouteOpenAPI() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestUnitController>()
        val router = APIUnitRouter(
            controller,
            ITestUnitController::class
        )
        val openAPI = OpenAPI()
        coEvery { controller.helloQuery("world") } returns "Hello world"
        routing {
            router.createRoutes(this, openAPI)
        }
        client.get("/api/hello/query?name=world")
        val get = openAPI.paths["/api/hello/query"]?.get
        assertEquals(1, get?.parameters?.size)
        assertEquals("name", get?.parameters?.firstOrNull()?.name)
        assertEquals("query", get?.parameters?.firstOrNull()?.`in`)
        assertEquals("kotlin.String", get?.parameters?.firstOrNull()?.schema?.type)
    }

    @Test
    fun testAPIPathParameterRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestUnitController>()
        val router = APIUnitRouter(
            controller,
            ITestUnitController::class
        )
        coEvery { controller.helloPath("world") } returns "Hello world"
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/hello/path/world")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello world", response.body())
    }

    @Test
    fun testAPIPathParameterRouteOpenAPI() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestUnitController>()
        val router = APIUnitRouter(
            controller,
            ITestUnitController::class
        )
        val openAPI = OpenAPI()
        coEvery { controller.helloPath("world") } returns "Hello world"
        routing {
            router.createRoutes(this, openAPI)
        }
        client.get("/api/hello/path/world")
        val get = openAPI.paths["/api/hello/path/{name}"]?.get
        assertEquals(1, get?.parameters?.size)
        assertEquals("name", get?.parameters?.firstOrNull()?.name)
        assertEquals("path", get?.parameters?.firstOrNull()?.`in`)
        assertEquals("kotlin.String", get?.parameters?.firstOrNull()?.schema?.type)
    }

    @Test
    fun testAPIBasicRouteCustomRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestUnitController>()
        val router = APIUnitRouter(
            controller,
            ITestUnitController::class,
            route = "/test"
        )
        coEvery { controller.hello() } returns "Hello world"
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/test/hello")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello world", response.body())
    }

    @Test
    fun testAPIStatusRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestUnitController>()
        val router = APIUnitRouter(
            controller,
            ITestUnitController::class
        )
        coEvery { controller.status() } returns StatusResponse(HttpStatusCode.Created, "Hello world")
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/status")
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals("Hello world", response.body())
    }

    @Test
    fun testAPIBytesRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestUnitController>()
        val router = APIUnitRouter(
            controller,
            ITestUnitController::class
        )
        val bytes = "Hello world".toByteArray()
        coEvery { controller.bytes() } returns BytesResponse(bytes, ContentType.Image.PNG, HttpStatusCode.Created)
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/bytes")
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(ContentType.Image.PNG, response.contentType())
        assertEquals(bytes.toList(), response.body<ByteArray>().toList())
    }

    @Test
    fun testAPIStreamRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestUnitController>()
        val router = APIUnitRouter(
            controller,
            ITestUnitController::class
        )
        val bytes = "Hello world".toByteArray()
        coEvery { controller.stream() } returns StreamResponse({
            write(bytes)
        }, ContentType.Image.PNG, HttpStatusCode.Created)
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/stream")
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(ContentType.Image.PNG, response.contentType())
        assertEquals(bytes.toList(), response.body<ByteArray>().toList())
    }

    @Test
    fun testAPIPathParameterUUIDRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestUnitController>()
        val router = APIUnitRouter(
            controller,
            ITestUnitController::class
        )
        val uuid = UUID()
        coEvery { controller.uuid(uuid) } returns uuid.toString()
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/uuid/$uuid")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(uuid.toString(), response.body())
    }

    @Test
    fun testAPIPathParameterWrongUUIDRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestUnitController>()
        val router = APIUnitRouter(
            controller,
            ITestUnitController::class
        )
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/uuid/wrong")
        assertEquals(HttpStatusCode.NotFound, response.status)
    }

}
