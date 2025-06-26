package dev.kaccelero.routers

import dev.kaccelero.commons.exceptions.ControllerException
import dev.kaccelero.controllers.IModelController
import dev.kaccelero.mocks.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.testing.*
import io.ktor.util.reflect.*
import io.mockk.coEvery
import io.mockk.mockk
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class APIModelRouterTest {

    private val mock = TestModel(1, "string")
    private val createMock = TestCreatePayload("string")
    private val updateMock = TestUpdatePayload("string")
    private val createMockInvalid = TestCreatePayload("123")
    private val updateMockInvalid = TestUpdatePayload("123")

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

    private fun createRouter(
        controller: IModelController<TestModel, Long, TestCreatePayload, TestUpdatePayload>,
    ) = APIModelRouter(
        typeInfo<TestModel>(),
        typeInfo<TestCreatePayload>(),
        typeInfo<TestUpdatePayload>(),
        controller,
        ITestModelController::class
    )

    @Test
    fun testAPIBasicRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        coEvery { controller.basic(any()) } returns "Hello world"
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/testmodels/basic")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Hello world", response.body())
    }

    @Test
    fun testAPIBasicRouteOpenAPI() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        val openAPI = OpenAPI()
        coEvery { controller.basic(any()) } returns "Hello world"
        routing {
            router.createRoutes(this, openAPI)
        }
        client.get("/api/testmodels/basic")
        val get = openAPI.paths["/api/testmodels/basic"]?.get
        assertEquals("basic", get?.operationId)
        assertEquals(listOf("TestModel"), get?.tags)
        assertEquals("Basic test", get?.description)
        assertEquals(0, get?.parameters?.size)
        assertEquals(1, get?.responses?.size)
        assertEquals(
            "#/components/schemas/${String::class.qualifiedName}",
            get?.responses?.get("200")?.content?.get("application/json")?.schema?.`$ref`
        )
    }

    @Test
    fun testAPIBasicMapRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        coEvery { controller.basicMap(any()) } returns mapOf("key" to "Hello world")
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/testmodels/basic/map")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(mapOf("key" to "Hello world"), response.body())
    }

    @Test
    fun testAPIBasicModelRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        coEvery { controller.basicModel(any()) } returns TestUser("userId")
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/testmodels/basic/model")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(TestUser("userId"), response.body())
    }

    @Test
    fun testAPIGetRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        coEvery { controller.list(any()) } returns listOf(mock)
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/testmodels")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(listOf(mock), response.body())
    }

    @Test
    fun testAPIGetRouteControllerException() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        coEvery { controller.list(any()) } throws ControllerException(
            HttpStatusCode.NotFound,
            "error_mock"
        )
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/testmodels")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(mapOf("error" to "error_mock"), response.body())
    }

    @Test
    fun testAPIGetRouteOpenAPI() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        val openAPI = OpenAPI()
        coEvery { controller.list(any()) } returns listOf(mock)
        routing {
            router.createRoutes(this, openAPI)
        }
        client.get("/api/testmodels")
        val get = openAPI.paths["/api/testmodels"]?.get
        assertEquals("listTestModel", get?.operationId)
        assertEquals(listOf("TestModel"), get?.tags)
        assertEquals("Get all TestModel", get?.description)
        assertEquals(0, get?.parameters?.size)
        assertEquals(1, get?.responses?.size)
        assertEquals(
            "#/components/schemas/${TestModel::class.qualifiedName}",
            get?.responses?.get("200")?.content?.get("application/json")?.schema?.items?.`$ref`
        )
    }

    @Test
    fun testAPIGetIdRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        coEvery { controller.get(any(), 1) } returns mock
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/testmodels/1")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(mock, response.body())
    }

    @Test
    fun testAPIGetIdRouteControllerException() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        coEvery { controller.get(any(), 1) } throws ControllerException(
            HttpStatusCode.NotFound,
            "error_mock"
        )
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/testmodels/1")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(mapOf("error" to "error_mock"), response.body())
    }

    @Test
    fun testAPIGetIdRouteOpenAPI() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        val openAPI = OpenAPI()
        coEvery { controller.get(any(), 1) } returns mock
        routing {
            router.createRoutes(this, openAPI)
        }
        client.get("/api/testmodels/1")
        val get = openAPI.paths["/api/testmodels/{testmodelId}"]?.get
        assertEquals("getTestModel", get?.operationId)
        assertEquals(listOf("TestModel"), get?.tags)
        assertEquals("Get a TestModel by id", get?.description)
        assertEquals(1, get?.parameters?.size)
        assertEquals("testmodelId", get?.parameters?.firstOrNull()?.name)
        assertEquals(2, get?.responses?.size)
        assertEquals(
            "#/components/schemas/${TestModel::class.qualifiedName}",
            get?.responses?.get("200")?.content?.get("application/json")?.schema?.`$ref`
        )
        assertEquals(
            Schema<String>().type("string").example("testmodels_not_found"),
            get?.responses?.get("404")?.content?.get("application/json")?.schema?.properties?.get("error")
        )
    }

    @Test
    fun testAPIPostRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        coEvery { controller.create(any(), createMock) } returns mock
        routing {
            router.createRoutes(this)
        }
        val response = client.post("/api/testmodels") {
            contentType(ContentType.Application.Json)
            setBody(createMock)
        }
        assertEquals(HttpStatusCode.Created, response.status)
        assertEquals(mock, response.body())
    }

    @Test
    fun testAPIPostRouteControllerException() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        coEvery { controller.create(any(), createMock) } throws ControllerException(
            HttpStatusCode.NotFound,
            "error_mock"
        )
        routing {
            router.createRoutes(this)
        }
        val response = client.post("/api/testmodels") {
            contentType(ContentType.Application.Json)
            setBody(createMock)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(mapOf("error" to "error_mock"), response.body())
    }

    @Test
    fun testAPIPostRouteValidatorException() = testApplication {
        val client = installApp(this)
        val router = createRouter(mockk())
        routing {
            router.createRoutes(this)
        }
        val response = client.post("/api/testmodels") {
            contentType(ContentType.Application.Json)
            setBody(createMockInvalid)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(mapOf("error" to "testmodels_string_regex"), response.body())
    }

    @Test
    fun testAPIPostRouteInvalidBody() = testApplication {
        val client = installApp(this)
        val router = createRouter(mockk())
        routing {
            router.createRoutes(this)
        }
        val response = client.post("/api/testmodels") {
            setBody("invalid")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(mapOf("error" to "error_body_invalid"), response.body())
    }

    @Test
    fun testAPIPostRouteInvalidJson() = testApplication {
        val client = installApp(this)
        val router = createRouter(mockk())
        routing {
            router.createRoutes(this)
        }
        val response = client.post("/api/testmodels") {
            contentType(ContentType.Application.Json)
            setBody("{}")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(
            mapOf("error" to "Illegal input: Field 'string' is required for type with serial name 'dev.kaccelero.mocks.TestCreatePayload', but it was missing at path: $"),
            response.body()
        )
    }

    @Test
    fun testAPIPostRouteOpenAPI() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        val openAPI = OpenAPI()
        coEvery { controller.create(any(), createMock) } returns mock
        routing {
            router.createRoutes(this, openAPI)
        }
        client.post("/api/testmodels") {
            contentType(ContentType.Application.Json)
            setBody(createMock)
        }
        val post = openAPI.paths["/api/testmodels"]?.post
        assertEquals("createTestModel", post?.operationId)
        assertEquals(listOf("TestModel"), post?.tags)
        assertEquals("Create a TestModel", post?.description)
        assertEquals(0, post?.parameters?.size)
        assertEquals(
            "#/components/schemas/${TestCreatePayload::class.qualifiedName}",
            post?.requestBody?.content?.get("application/json")?.schema?.`$ref`
        )
        assertEquals(2, post?.responses?.size)
        assertEquals(
            "#/components/schemas/${TestModel::class.qualifiedName}",
            post?.responses?.get("201")?.content?.get("application/json")?.schema?.`$ref`
        )
        assertEquals(
            Schema<String>().type("string").example("error_body_invalid"),
            post?.responses?.get("400")?.content?.get("application/json")?.schema?.properties?.get("error")
        )
    }

    @Test
    fun testAPIPutRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        coEvery { controller.update(any(), 1, updateMock) } returns mock
        routing {
            router.createRoutes(this)
        }
        val response = client.put("/api/testmodels/1") {
            contentType(ContentType.Application.Json)
            setBody(updateMock)
        }
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(mock, response.body())
    }

    @Test
    fun testAPIPutRouteControllerException() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        coEvery { controller.update(any(), 1, updateMock) } throws ControllerException(
            HttpStatusCode.NotFound,
            "error_mock"
        )
        routing {
            router.createRoutes(this)
        }
        val response = client.put("/api/testmodels/1") {
            contentType(ContentType.Application.Json)
            setBody(updateMock)
        }
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(mapOf("error" to "error_mock"), response.body())
    }

    @Test
    fun testAPIPutRouteValidatorException() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        coEvery { controller.update(any(), 1, updateMock) } returns mock
        routing {
            router.createRoutes(this)
        }
        val response = client.put("/api/testmodels/1") {
            contentType(ContentType.Application.Json)
            setBody(updateMockInvalid)
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(mapOf("error" to "testmodels_string_regex"), response.body())
    }

    @Test
    fun testAPIPutRouteInvalidBody() = testApplication {
        val client = installApp(this)
        val router = createRouter(mockk())
        routing {
            router.createRoutes(this)
        }
        val response = client.put("/api/testmodels/1") {
            setBody("invalid")
        }
        assertEquals(HttpStatusCode.BadRequest, response.status)
        assertEquals(mapOf("error" to "error_body_invalid"), response.body())
    }

    @Test
    fun testAPIPutRouteOpenAPI() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        val openAPI = OpenAPI()
        coEvery { controller.update(any(), 1, updateMock) } returns mock
        routing {
            router.createRoutes(this, openAPI)
        }
        client.put("/api/testmodels/1") {
            contentType(ContentType.Application.Json)
            setBody(updateMock)
        }
        val put = openAPI.paths["/api/testmodels/{testmodelId}"]?.put
        assertEquals("updateTestModel", put?.operationId)
        assertEquals(listOf("TestModel"), put?.tags)
        assertEquals("Update a TestModel by id", put?.description)
        assertEquals(1, put?.parameters?.size)
        assertEquals("testmodelId", put?.parameters?.firstOrNull()?.name)
        assertEquals(
            "#/components/schemas/${TestUpdatePayload::class.qualifiedName}",
            put?.requestBody?.content?.get("application/json")?.schema?.`$ref`
        )
        assertEquals(2, put?.responses?.size)
        assertEquals(
            "#/components/schemas/${TestModel::class.qualifiedName}",
            put?.responses?.get("200")?.content?.get("application/json")?.schema?.`$ref`
        )
        assertEquals(
            Schema<String>().type("string").example("error_body_invalid"),
            put?.responses?.get("400")?.content?.get("application/json")?.schema?.properties?.get("error")
        )
    }

    @Test
    fun testAPIDeleteRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        coEvery { controller.delete(any(), 1) } returns Unit
        routing {
            router.createRoutes(this)
        }
        val response = client.delete("/api/testmodels/1")
        assertEquals(HttpStatusCode.NoContent, response.status)
    }

    @Test
    fun testAPIDeleteRouteControllerException() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        coEvery { controller.delete(any(), 1) } throws ControllerException(
            HttpStatusCode.NotFound,
            "error_mock"
        )
        routing {
            router.createRoutes(this)
        }
        val response = client.delete("/api/testmodels/1")
        assertEquals(HttpStatusCode.NotFound, response.status)
        assertEquals(mapOf("error" to "error_mock"), response.body())
    }

    @Test
    fun testAPIDeleteRouteOpenAPI() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        val openAPI = OpenAPI()
        coEvery { controller.delete(any(), 1) } returns Unit
        routing {
            router.createRoutes(this, openAPI)
        }
        client.delete("/api/testmodels/1")
        val delete = openAPI.paths["/api/testmodels/{testmodelId}"]?.delete
        assertEquals("deleteTestModel", delete?.operationId)
        assertEquals(listOf("TestModel"), delete?.tags)
        assertEquals("Delete a TestModel by id", delete?.description)
        assertEquals(1, delete?.parameters?.size)
        assertEquals("testmodelId", delete?.parameters?.firstOrNull()?.name)
        assertEquals(1, delete?.responses?.size)
        assertEquals("No content", delete?.responses?.get("204")?.description)
        assertEquals(null, delete?.responses?.get("204")?.content)
    }

    @Test
    fun testAPIGetRecursiveRoute() = testApplication {
        val client = installApp(this)
        val controller = mockk<ITestModelController>()
        val router = createRouter(controller)
        val recursiveModel = TestRecursiveModel("name", listOf(TestRecursiveModel("child")))
        coEvery { controller.recursive(any()) } returns recursiveModel
        routing {
            router.createRoutes(this)
        }
        val response = client.get("/api/testmodels/recursive")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals(recursiveModel, response.body())
    }

}
