package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.usecases.ITripleUseCase

interface IUpdateChildModelUseCase<Model : IChildModel<Id, *, UpdatePayload, ParentId>, Id, UpdatePayload, ParentId> :
    ITripleUseCase<Id, UpdatePayload, ParentId, Model?>
