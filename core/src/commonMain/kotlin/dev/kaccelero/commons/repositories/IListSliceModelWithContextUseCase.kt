package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IContext
import dev.kaccelero.models.IModel
import dev.kaccelero.repositories.Pagination
import kotlin.js.JsName

interface IListSliceModelWithContextUseCase<Model : IModel<*, *, *>> :
    IListSliceChildModelWithContextUseCase<Model, Unit> {

    @JsName("invokeDefault")
    operator fun invoke(input1: Pagination, input2: IContext): List<Model>

    override fun invoke(input1: Pagination, input2: Unit, input3: IContext): List<Model> = invoke(input1, input3)

}
