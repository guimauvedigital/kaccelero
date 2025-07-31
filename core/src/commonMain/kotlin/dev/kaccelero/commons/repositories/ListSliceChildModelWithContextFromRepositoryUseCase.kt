package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.models.IContext
import dev.kaccelero.repositories.IChildModelRepository
import dev.kaccelero.repositories.Pagination

open class ListSliceChildModelWithContextFromRepositoryUseCase<Model : IChildModel<*, *, *, ParentId>, ParentId>(
    private val repository: IChildModelRepository<Model, *, *, *, ParentId>,
) : IListSliceChildModelWithContextUseCase<Model, ParentId> {

    override fun invoke(input1: Pagination, input2: ParentId, input3: IContext): List<Model> =
        repository.list(input1, input2, input3)

}
