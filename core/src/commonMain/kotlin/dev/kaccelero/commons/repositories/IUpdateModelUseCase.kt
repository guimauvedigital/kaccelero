package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IModel
import kotlin.js.JsName

interface IUpdateModelUseCase<Model : IModel<Id, *, UpdatePayload>, Id, UpdatePayload> :
    IUpdateChildModelUseCase<Model, Id, UpdatePayload, Unit> {

    @JsName("invokeDefault")
    operator fun invoke(input1: Id, input2: UpdatePayload): Model?

    override fun invoke(input1: Id, input2: UpdatePayload, input3: Unit): Model? = invoke(input1, input2)

}
