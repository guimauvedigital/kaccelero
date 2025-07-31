package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.models.IContext
import dev.kaccelero.usecases.IQuadUseCase

interface IUpdateChildModelWithContextUseCase<Model : IChildModel<Id, *, UpdatePayload, ParentId>, Id, UpdatePayload, ParentId> :
    IQuadUseCase<Id, UpdatePayload, ParentId, IContext, Model?>
