package dev.kaccelero.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.models.IContext
import kotlin.js.JsName

interface IChildModelRepository<Model : IChildModel<Id, CreatePayload, UpdatePayload, ParentId>, Id, CreatePayload, UpdatePayload, ParentId> {

    fun list(parentId: ParentId, context: IContext? = null): List<Model> = throw UnsupportedOperationException()

    @JsName("listWithLimit")
    fun list(pagination: Pagination, parentId: ParentId, context: IContext? = null): List<Model> =
        throw UnsupportedOperationException()

    fun count(parentId: ParentId, context: IContext? = null): Long = throw UnsupportedOperationException()

    fun get(id: Id, parentId: ParentId, context: IContext? = null): Model? = throw UnsupportedOperationException()

    fun create(payload: CreatePayload, parentId: ParentId, context: IContext? = null): Model? =
        throw UnsupportedOperationException()

    fun update(id: Id, payload: UpdatePayload, parentId: ParentId, context: IContext? = null): Boolean =
        throw UnsupportedOperationException()

    fun delete(id: Id, parentId: ParentId, context: IContext? = null): Boolean = throw UnsupportedOperationException()

}
