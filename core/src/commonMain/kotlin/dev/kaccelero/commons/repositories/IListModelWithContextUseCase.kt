package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IContext
import dev.kaccelero.models.IModel
import kotlin.js.JsName

interface IListModelWithContextUseCase<Model : IModel<*, *, *>> : IListChildModelWithContextUseCase<Model, Unit> {

    @JsName("invokeDefault")
    operator fun invoke(input: IContext): List<Model>

    override fun invoke(input1: Unit, input2: IContext): List<Model> = invoke(input2)

}
