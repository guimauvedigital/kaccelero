package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.models.IContext
import dev.kaccelero.repositories.Pagination
import dev.kaccelero.usecases.ITripleUseCase

interface IListSliceChildModelWithContextUseCase<Model : IChildModel<*, *, *, ParentId>, ParentId> :
    ITripleUseCase<Pagination, ParentId, IContext, List<Model>>
