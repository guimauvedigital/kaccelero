package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IContext
import dev.kaccelero.models.IModel
import kotlin.js.JsName

interface ICreateModelWithContextUseCase<Model : IModel<*, CreatePayload, *>, CreatePayload> :
    ICreateChildModelWithContextUseCase<Model, CreatePayload, Unit> {

    @JsName("invokeDefault")
    operator fun invoke(input1: CreatePayload, input2: IContext): Model?

    override fun invoke(input1: CreatePayload, input2: Unit, input3: IContext): Model? = invoke(input1, input3)

}
