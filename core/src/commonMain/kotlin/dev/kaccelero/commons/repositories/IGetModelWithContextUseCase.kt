package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IContext
import dev.kaccelero.models.IModel
import kotlin.js.JsName

interface IGetModelWithContextUseCase<Model : IModel<Id, *, *>, Id> :
    IGetChildModelWithContextUseCase<Model, Id, Unit> {

    @JsName("invokeDefault")
    operator fun invoke(input1: Id, input2: IContext): Model?

    override fun invoke(input1: Id, input2: Unit, input3: IContext): Model? = invoke(input1, input3)

}
