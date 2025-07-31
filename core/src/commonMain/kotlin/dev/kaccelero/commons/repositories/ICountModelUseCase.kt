package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IModel
import kotlin.js.JsName

interface ICountModelUseCase<Model : IModel<*, *, *>> : ICountChildModelUseCase<Model, Unit> {

    @JsName("invokeDefault")
    operator fun invoke(): Long

    override fun invoke(input: Unit): Long = invoke()

}
