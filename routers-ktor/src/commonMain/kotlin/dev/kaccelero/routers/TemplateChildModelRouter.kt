package dev.kaccelero.routers

import dev.kaccelero.annotations.*
import dev.kaccelero.commons.exceptions.ControllerException
import dev.kaccelero.commons.responses.ControllerResponse
import dev.kaccelero.commons.responses.RedirectResponse
import dev.kaccelero.controllers.IChildModelController
import dev.kaccelero.models.IChildModel
import dev.kaccelero.models.UnitModel
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.*
import io.ktor.util.reflect.*
import io.swagger.v3.oas.models.OpenAPI
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
open class TemplateChildModelRouter<Model : IChildModel<Id, CreatePayload, UpdatePayload, ParentId>, Id, CreatePayload : Any, UpdatePayload : Any, ParentModel : IChildModel<ParentId, *, *, *>, ParentId>(
    modelTypeInfo: TypeInfo,
    createPayloadTypeInfo: TypeInfo,
    updatePayloadTypeInfo: TypeInfo,
    controller: IChildModelController<Model, Id, CreatePayload, UpdatePayload, ParentModel, ParentId>,
    controllerClass: KClass<out IChildModelController<Model, Id, CreatePayload, UpdatePayload, ParentModel, ParentId>>,
    parentRouter: IChildModelRouter<ParentModel, ParentId, *, *, *, *>?,
    val respondTemplate: suspend ApplicationCall.(String, Map<String, Any?>) -> Unit,
    val errorTemplate: String? = null,
    val redirectUnauthorizedToUrl: String? = null,
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
    prefix
) {

    open suspend fun handleExceptionTemplate(
        exception: Exception,
        call: ApplicationCall,
        fromTemplate: String,
    ) {
        when (exception) {
            is RedirectResponse -> exception.respond(call)

            is ControllerException -> {
                redirectUnauthorizedToUrl?.takeIf {
                    exception.code == HttpStatusCode.Unauthorized && !isUnauthorizedRedirectPath(call)
                }?.let { url ->
                    call.respondRedirect(url.replace("{path}", call.request.uri))
                    return
                }
                call.response.status(exception.code)
                call.respondTemplate(
                    errorTemplate ?: fromTemplate,
                    mapOf(
                        "route" to route,
                        "code" to exception.code.value,
                        "error" to exception.key
                    )
                )
            }

            is MissingParameterException -> handleExceptionTemplate(
                ControllerException(
                    if (exception.type.isBadRequest) HttpStatusCode.BadRequest else HttpStatusCode.NotFound,
                    "error_parameter_${exception.key}"
                ), call, fromTemplate
            )

            is PropertyValidatorException -> handleExceptionTemplate(
                ControllerException(
                    HttpStatusCode.BadRequest, "${route}_${exception.key}_${exception.reason}"
                ), call, fromTemplate
            )

            else -> throw exception
        }
    }

    open fun isUnauthorizedRedirectPath(call: ApplicationCall): Boolean {
        return redirectUnauthorizedToUrl?.startsWith(call.request.path()) == true
    }

    override suspend fun <Payload : Any> decodePayload(call: ApplicationCall, type: KClass<Payload>): Payload {
        return ModelAnnotations.constructPayloadFromStringLists(
            type, call.receiveParameters().toMap()
        ) ?: throw ControllerException(HttpStatusCode.BadRequest, "error_body_invalid")
    }

    override fun createControllerRoute(root: Route, controllerRoute: ControllerRoute, openAPI: OpenAPI?) {
        val mapping = controllerRoute.annotations.firstNotNullOfOrNull { it as? TemplateMapping } ?: return

        // Calculate route (path, keys, ...)
        val method = controllerRoute.method ?: HttpMethod.Get
        val path = ("/" + (controllerRoute.path ?: when (controllerRoute.type) {
            RouteType.getModel -> "{$id}"
            RouteType.createModel -> "create"
            RouteType.updateModel -> "{$id}/update"
            RouteType.deleteModel -> "{$id}/delete"
            else -> ""
        }).removePrefix("/")).removeSuffix("/")
        val keys = when (controllerRoute.type) {
            RouteType.createModel -> createPayloadKeys
            RouteType.updateModel -> updatePayloadKeys
            else -> modelKeys
        }
        val isForm = when (controllerRoute.type) {
            RouteType.createModel, RouteType.updateModel, RouteType.deleteModel -> true
            else -> false
        }
        val hasPayload = controllerRoute.parameters.any { parameter ->
            parameter.annotations.any { it is Payload }
        }

        root.route(fullRoute + path, method) {
            handle {
                try {
                    val item = when {
                        isForm && path.contains("{$id}") -> get(call)
                        !hasPayload || method != HttpMethod.Get -> invokeControllerRoute(call, controllerRoute)
                        else -> null
                    }.takeIf { it != Unit && it != UnitModel }

                    if (item is ControllerResponse) return@handle item.respond(call)

                    val itemMap = item as? Map<String, *>
                        ?: if (item == null) mapOf()
                        else if (controllerRoute.type == RouteType.listModel) mapOf("items" to item)
                        else mapOf("item" to item)

                    call.respondTemplate(
                        mapping.template,
                        mapOf("route" to route, "keys" to keys) + itemMap
                    )
                } catch (exception: Exception) {
                    handleExceptionTemplate(exception, call, mapping.template)
                }
            }
        }
        if (isForm) {
            root.post(fullRoute + path) {
                try {
                    invokeControllerRoute(call, controllerRoute)
                    call.respondRedirect(
                        "../".repeat(path.count { it == '/' }) + route
                    )
                } catch (exception: Exception) {
                    handleExceptionTemplate(exception, call, mapping.template)
                }
            }
        }
    }

}
