package dev.kaccelero.repositories

import dev.kaccelero.models.IContext
import dev.kaccelero.models.IModel
import kotlin.js.JsName

interface IModelRepository<Model : IModel<Id, CreatePayload, UpdatePayload>, Id, CreatePayload, UpdatePayload> :
    IChildModelRepository<Model, Id, CreatePayload, UpdatePayload, Unit> {

    @JsName("listDefault")
    fun list(context: IContext? = null): List<Model> = throw UnsupportedOperationException()

    @JsName("listWithLimitDefault")
    fun list(pagination: Pagination, context: IContext? = null): List<Model> = throw UnsupportedOperationException()

    @JsName("countDefault")
    fun count(context: IContext? = null): Long = throw UnsupportedOperationException()

    @JsName("getDefault")
    fun get(id: Id, context: IContext? = null): Model? = throw UnsupportedOperationException()

    @JsName("createDefault")
    fun create(payload: CreatePayload, context: IContext? = null): Model? = throw UnsupportedOperationException()

    @JsName("updateDefault")
    fun update(id: Id, payload: UpdatePayload, context: IContext? = null): Boolean =
        throw UnsupportedOperationException()

    @JsName("deleteDefault")
    fun delete(id: Id, context: IContext? = null): Boolean = throw UnsupportedOperationException()

    override fun list(parentId: Unit, context: IContext?): List<Model> = list(context)

    override fun list(pagination: Pagination, parentId: Unit, context: IContext?): List<Model> =
        list(pagination, context)

    override fun count(parentId: Unit, context: IContext?): Long = count(context)

    override fun get(id: Id, parentId: Unit, context: IContext?): Model? = get(id, context)

    override fun create(payload: CreatePayload, parentId: Unit, context: IContext?): Model? = create(payload, context)

    override fun update(id: Id, payload: UpdatePayload, parentId: Unit, context: IContext?): Boolean =
        update(id, payload, context)

    override fun delete(id: Id, parentId: Unit, context: IContext?): Boolean = delete(id, context)

}
