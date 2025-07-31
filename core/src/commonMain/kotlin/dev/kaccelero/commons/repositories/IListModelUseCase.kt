package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IModel
import kotlin.js.JsName

interface IListModelUseCase<Model : IModel<*, *, *>> : IListChildModelUseCase<Model, Unit> {

    @JsName("invokeDefault")
    operator fun invoke(): List<Model>

    override fun invoke(input: Unit): List<Model> = invoke()

}
