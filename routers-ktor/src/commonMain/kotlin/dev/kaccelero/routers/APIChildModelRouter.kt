package dev.kaccelero.routers

import dev.kaccelero.annotations.*
import dev.kaccelero.commons.exceptions.ControllerException
import dev.kaccelero.commons.responses.ControllerResponse
import dev.kaccelero.commons.responses.RedirectResponse
import dev.kaccelero.commons.responses.StatusResponse
import dev.kaccelero.controllers.IChildModelController
import dev.kaccelero.models.IChildModel
import dev.kaccelero.models.UnitModel
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.reflect.*
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.media.Schema
import io.swagger.v3.oas.models.parameters.Parameter
import kotlin.reflect.KClass
import kotlin.reflect.typeOf

open class APIChildModelRouter<Model : IChildModel<Id, CreatePayload, UpdatePayload, ParentId>, Id, CreatePayload : Any, UpdatePayload : Any, ParentModel : IChildModel<ParentId, *, *, *>, ParentId>(
    modelTypeInfo: TypeInfo,
    createPayloadTypeInfo: TypeInfo,
    updatePayloadTypeInfo: TypeInfo,
    controller: IChildModelController<Model, Id, CreatePayload, UpdatePayload, ParentModel, ParentId>,
    controllerClass: KClass<out IChildModelController<Model, Id, CreatePayload, UpdatePayload, ParentModel, ParentId>>,
    parentRouter: IChildModelRouter<ParentModel, *, *, *, *, *>?,
    route: String? = null,
    id: String? = null,
    prefix: String? = null,
) : AbstractChildModelRouter<Model, Id, CreatePayload, UpdatePayload, ParentModel, ParentId>(
    modelTypeInfo,
    createPayloadTypeInfo,
    updatePayloadTypeInfo,
    controller,
    controllerClass,
    parentRouter,
    route,
    id,
    prefix ?: "/api"
) {

    open suspend fun handleExceptionAPI(exception: Exception, call: ApplicationCall) {
        when (exception) {
            is RedirectResponse -> exception.respond(call)

            is ControllerException -> {
                call.response.status(exception.code)
                call.respond(mapOf("error" to exception.key))
            }

            is MissingParameterException -> handleExceptionAPI(
                ControllerException(
                    if (exception.type.isBadRequest) HttpStatusCode.BadRequest else HttpStatusCode.NotFound,
                    "error_parameter_${exception.key}"
                ), call
            )

            is PropertyValidatorException -> handleExceptionAPI(
                ControllerException(HttpStatusCode.BadRequest, "${route}_${exception.key}_${exception.reason}"), call
            )

            is ContentTransformationException -> handleExceptionAPI(
                ControllerException(HttpStatusCode.BadRequest, "error_body_invalid"), call
            )

            else -> throw exception
        }
    }

    override suspend fun <Payload : Any> decodePayload(call: ApplicationCall, type: KClass<Payload>): Payload {
        return call.receive(type)
    }

    open fun getControllerRoutePath(controllerRoute: ControllerRoute) =
        ("/" + (controllerRoute.path ?: when (controllerRoute.type) {
            RouteType.getModel, RouteType.updateModel, RouteType.deleteModel -> "{$id}"
            else -> ""
        }).removePrefix("/")).removeSuffix("/")

    open fun getControllerRouteMethod(controllerRoute: ControllerRoute) =
        controllerRoute.method ?: when (controllerRoute.type) {
            RouteType.createModel -> HttpMethod.Post
            RouteType.updateModel -> HttpMethod.Put
            RouteType.deleteModel -> HttpMethod.Delete
            else -> HttpMethod.Get
        }

    override fun createControllerRoute(root: Route, controllerRoute: ControllerRoute, openAPI: OpenAPI?) {
        val apiMapping = controllerRoute.annotations.firstNotNullOfOrNull { it as? APIMapping } ?: return
        val path = getControllerRoutePath(controllerRoute)
        val method = getControllerRouteMethod(controllerRoute)

        // Route handling
        root.route(fullRoute + path, method) {
            handle {
                try {
                    val response = invokeControllerRoute(call, controllerRoute)
                    val item = when (response) {
                        is ControllerResponse -> return@handle response.respond(call)
                        is StatusResponse<*> -> response.content
                        else -> response
                    }.takeIf { it != Unit && it != UnitModel }
                    val status = when {
                        response is StatusResponse<*> -> response.status
                        controllerRoute.type == RouteType.createModel -> HttpStatusCode.Created
                        item == null -> HttpStatusCode.NoContent
                        else -> HttpStatusCode.OK
                    }
                    item?.let { call.respond(status, item) } ?: call.respond(status)
                } catch (exception: Exception) {
                    handleExceptionAPI(exception, call)
                }
            }
        }

        // API docs
        openAPI?.route(method, fullRoute + path) {
            val type = controllerRoute.returnType
            val isUnitType = type == typeOf<Unit>() || type == typeOf<UnitModel>()
            val documentedType = controllerRoute.annotations.firstNotNullOfOrNull {
                it as? DocumentedType
            }?.type ?: type.underlyingType?.classifier as? KClass<*>
            val documentedTypeName = documentedType?.simpleName ?: documentedType.toString()
            val documentedTag = controllerRoute.annotations.firstNotNullOfOrNull { it as? DocumentedTag }?.name
                ?: modelTypeInfo.type.simpleName

            // General metadata
            addTagsItem(documentedTag)
            (apiMapping.operationId.takeIf { it.isNotEmpty() } ?: when (controllerRoute.type) {
                RouteType.listModel -> "list$documentedTypeName"
                RouteType.getModel -> "get$documentedTypeName"
                RouteType.createModel -> "create$documentedTypeName"
                RouteType.updateModel -> "update$documentedTypeName"
                RouteType.deleteModel -> "delete$documentedTypeName"
                else -> null
            })?.let { operationId(it) }
            (apiMapping.description.takeIf { it.isNotEmpty() } ?: when (controllerRoute.type) {
                RouteType.listModel -> "Get all $documentedTypeName"
                RouteType.getModel -> "Get a $documentedTypeName by id"
                RouteType.createModel -> "Create a $documentedTypeName"
                RouteType.updateModel -> "Update a $documentedTypeName by id"
                RouteType.deleteModel -> "Delete a $documentedTypeName by id"
                else -> null
            })?.let { description(it) }
            parameters(
                getOpenAPIParameters(path.contains("{$id}")) + controllerRoute.parameters.mapNotNull {
                    Parameter()
                        .`in`(
                            if (it.annotations.any { annotation -> annotation is PathParameter }) "path"
                            else if (it.annotations.any { annotation -> annotation is QueryParameter }) "query"
                            else return@mapNotNull null
                        )
                        .name(it.name)
                        .schema(Schema<Any>().type(it.type.toString()))
                        .required(!it.type.isMarkedNullable)
                }
            )

            // Body and response linked to payload
            controllerRoute.parameters.singleOrNull {
                it.annotations.any { annotation -> annotation is Payload }
            }?.let {
                requestBody {
                    mediaType("application/json") {
                        schema(it.type, openAPI)
                    }
                }
                response("400") {
                    description("Invalid body")
                    mediaType("application/json") {
                        errorSchema("error_body_invalid")
                    }
                }
            }

            // Default response (direct return type of the function)
            response(
                if (isUnitType) "204"
                else if (controllerRoute.type == RouteType.createModel) "201"
                else "200"
            ) {
                if (isUnitType) {
                    description("No content")
                    return@response
                }
                description(type)
                mediaType("application/json") {
                    schema(type, openAPI)
                }
            }

            // Additional responses
            controllerRoute.annotations.filterIsInstance<DocumentedError>().forEach {
                response(it.code.toString()) {
                    description(it.description.takeIf { d -> d.isNotEmpty() } ?: it.key)
                    mediaType("application/json") {
                        errorSchema(it.key)
                    }
                }
            }
        }
    }

}
