package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.repositories.Pagination
import dev.kaccelero.usecases.IPairUseCase

interface IListSliceChildModelUseCase<Model : IChildModel<*, *, *, ParentId>, ParentId> :
    IPairUseCase<Pagination, ParentId, List<Model>>
