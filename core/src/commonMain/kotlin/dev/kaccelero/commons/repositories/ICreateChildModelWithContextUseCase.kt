package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.models.IContext
import dev.kaccelero.usecases.ITripleUseCase

interface ICreateChildModelWithContextUseCase<Model : IChildModel<*, CreatePayload, *, ParentId>, CreatePayload, ParentId> :
    ITripleUseCase<CreatePayload, ParentId, IContext, Model?>
