package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IContext
import dev.kaccelero.models.IModel
import kotlin.js.JsName

interface IUpdateModelWithContextUseCase<Model : IModel<Id, *, UpdatePayload>, Id, UpdatePayload> :
    IUpdateChildModelWithContextUseCase<Model, Id, UpdatePayload, Unit> {

    @JsName("invokeDefault")
    operator fun invoke(input1: Id, input2: UpdatePayload, input3: IContext): Model?

    override fun invoke(input1: Id, input2: UpdatePayload, input3: Unit, input4: IContext): Model? =
        invoke(input1, input2, input4)

}
