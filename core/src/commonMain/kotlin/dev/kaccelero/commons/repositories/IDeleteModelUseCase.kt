package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IModel
import kotlin.js.JsName

interface IDeleteModelUseCase<Model : IModel<Id, *, *>, Id> : IDeleteChildModelUseCase<Model, Id, Unit> {

    @JsName("invokeDefault")
    operator fun invoke(input: Id): Boolean

    override fun invoke(input1: Id, input2: Unit): Boolean = invoke(input1)

}
