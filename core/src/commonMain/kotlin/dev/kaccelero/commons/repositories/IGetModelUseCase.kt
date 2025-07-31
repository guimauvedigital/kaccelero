package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IModel
import kotlin.js.JsName

interface IGetModelUseCase<Model : IModel<Id, *, *>, Id> : IGetChildModelUseCase<Model, Id, Unit> {

    @JsName("invokeDefault")
    operator fun invoke(input: Id): Model?

    override fun invoke(input1: Id, input2: Unit): Model? = invoke(input1)

}
