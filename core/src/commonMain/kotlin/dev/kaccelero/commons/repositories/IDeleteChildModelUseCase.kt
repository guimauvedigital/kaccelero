package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.usecases.IPairUseCase
import kotlin.js.JsExport

@JsExport
interface IDeleteChildModelUseCase<Model : IChildModel<Id, *, *, ParentId>, Id, ParentId> :
    IPairUseCase<Id, ParentId, Boolean>
