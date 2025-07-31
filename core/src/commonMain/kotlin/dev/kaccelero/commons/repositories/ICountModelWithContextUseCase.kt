package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IContext
import dev.kaccelero.models.IModel
import kotlin.js.JsName

interface ICountModelWithContextUseCase<Model : IModel<*, *, *>> : ICountChildModelWithContextUseCase<Model, Unit> {

    @JsName("invokeDefault")
    operator fun invoke(input: IContext): Long

    override fun invoke(input1: Unit, input2: IContext): Long = invoke(input2)

}
