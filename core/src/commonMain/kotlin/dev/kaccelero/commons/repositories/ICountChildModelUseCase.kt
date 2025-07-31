package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.usecases.IUseCase

interface ICountChildModelUseCase<Model : IChildModel<*, *, *, ParentId>, ParentId> : IUseCase<ParentId, Long>
