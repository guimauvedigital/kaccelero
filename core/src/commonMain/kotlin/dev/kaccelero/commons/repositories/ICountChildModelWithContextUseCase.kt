package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.models.IContext
import dev.kaccelero.usecases.IPairUseCase

interface ICountChildModelWithContextUseCase<Model : IChildModel<*, *, *, ParentId>, ParentId> :
    IPairUseCase<ParentId, IContext, Long>
