package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IModel
import dev.kaccelero.repositories.Pagination
import kotlin.js.JsName

interface IListSliceModelUseCase<Model : IModel<*, *, *>> : IListSliceChildModelUseCase<Model, Unit> {

    @JsName("invokeDefault")
    operator fun invoke(input: Pagination): List<Model>

    override fun invoke(input1: Pagination, input2: Unit): List<Model> = invoke(input1)

}
