package dev.kaccelero.repositories

import dev.kaccelero.client.AbstractAPIClient
import dev.kaccelero.commons.exceptions.APIException
import dev.kaccelero.mocks.TestCreatePayload
import dev.kaccelero.mocks.TestModel
import dev.kaccelero.mocks.TestUpdatePayload
import io.ktor.client.engine.*
import io.ktor.client.engine.mock.*
import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.util.reflect.*
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.fail

class APIModelRemoteRepositoryTest {

    private fun createRepository(
        engine: HttpClientEngine,
    ): APIModelRemoteRepository<TestModel, Long, TestCreatePayload, TestUpdatePayload> {
        val client = object : AbstractAPIClient(
            "https://example.com",
            engine = engine
        ) {}
        return APIModelRemoteRepository(
            typeInfo<TestModel>(),
            typeInfo<TestCreatePayload>(),
            typeInfo<TestUpdatePayload>(),
            typeInfo<List<TestModel>>(),
            client
        )
    }

    @Test
    fun testList() = runBlocking {
        val repository = createRepository(MockEngine { request ->
            assertEquals("https://example.com/api/testmodels", request.url.toString())
            respond(
                content = """[{"id":1,"string":"string"}]""",
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val models = repository.list()
        assertEquals(1, models.size)
        assertEquals(1, models.first().id)
        assertEquals("string", models.first().string)
    }

    @Test
    fun testListWithOffsetLimit() = runBlocking {
        val repository = createRepository(MockEngine { request ->
            assertEquals("https://example.com/api/testmodels?limit=10&offset=5", request.url.toString())
            respond(
                content = """[{"id":1,"string":"string"}]""",
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val models = repository.list(Pagination(10, 5))
        assertEquals(1, models.size)
        assertEquals(1, models.first().id)
        assertEquals("string", models.first().string)
    }

    @Test
    fun testGet() = runBlocking {
        val repository = createRepository(MockEngine { request ->
            assertEquals("https://example.com/api/testmodels/1", request.url.toString())
            respond(
                content = """{"id":1,"string":"string"}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val model = repository.get(1) ?: fail("Model is null")
        assertEquals(1, model.id)
        assertEquals("string", model.string)
    }

    @Test
    fun testGetAPIError() = runBlocking {
        val repository = createRepository(MockEngine { request ->
            assertEquals("https://example.com/api/testmodels/1", request.url.toString())
            respond(
                content = """{"error": "not_found"}""",
                status = HttpStatusCode.NotFound,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val exception = assertFailsWith<APIException> {
            repository.get(1)
        }
        assertEquals(HttpStatusCode.NotFound, exception.code)
        assertEquals("not_found", exception.key)
    }

    @Test
    fun testGetAPIErrorInternal() = runBlocking {
        val repository = createRepository(MockEngine { request ->
            assertEquals("https://example.com/api/testmodels/1", request.url.toString())
            respond(
                content = """{"error": "internal_error"}""",
                status = HttpStatusCode.InternalServerError,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val exception = assertFailsWith<APIException> {
            repository.get(1)
        }
        assertEquals(HttpStatusCode.InternalServerError, exception.code)
        assertEquals("internal_error", exception.key)
    }

    @Test
    fun testCreate() = runBlocking {
        val repository = createRepository(MockEngine { request ->
            assertEquals("https://example.com/api/testmodels", request.url.toString())
            assertEquals(HttpMethod.Post, request.method)
            assertEquals(ContentType.Application.Json, request.body.contentType)
            assertEquals("""{"string":"string"}""", (request.body as TextContent).text)
            respond(
                content = """{"id":1,"string":"string"}""",
                status = HttpStatusCode.Created,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val model = repository.create(TestCreatePayload("string")) ?: fail("Model is null")
        assertEquals(1, model.id)
        assertEquals("string", model.string)
    }

    @Test
    fun testUpdate() = runBlocking {
        val repository = createRepository(MockEngine { request ->
            assertEquals("https://example.com/api/testmodels/1", request.url.toString())
            assertEquals(HttpMethod.Put, request.method)
            assertEquals(ContentType.Application.Json, request.body.contentType)
            assertEquals("""{"string":"string"}""", (request.body as TextContent).text)
            respond(
                content = """{"id":1,"string":"string"}""",
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        })
        val model = repository.update(1, TestUpdatePayload("string")) ?: fail("Model is null")
        assertEquals(1, model.id)
        assertEquals("string", model.string)
    }

    @Test
    fun testDelete() = runBlocking {
        val repository = createRepository(MockEngine { request ->
            assertEquals("https://example.com/api/testmodels/1", request.url.toString())
            assertEquals(HttpMethod.Delete, request.method)
            respond(
                content = "",
                status = HttpStatusCode.NoContent,
            )
        })
        assertEquals(true, repository.delete(1))
    }

}
