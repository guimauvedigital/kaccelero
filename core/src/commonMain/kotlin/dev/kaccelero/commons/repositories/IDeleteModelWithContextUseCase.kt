package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IContext
import dev.kaccelero.models.IModel
import kotlin.js.JsName

interface IDeleteModelWithContextUseCase<Model : IModel<Id, *, *>, Id> :
    IDeleteChildModelWithContextUseCase<Model, Id, Unit> {

    @JsName("invokeDefault")
    operator fun invoke(input1: Id, input2: IContext): Boolean

    override fun invoke(input1: Id, input2: Unit, input3: IContext): Boolean = invoke(input1, input3)

}
