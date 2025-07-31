package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.repositories.IChildModelRepository
import dev.kaccelero.repositories.Pagination

open class ListSliceChildModelFromRepositoryUseCase<Model : IChildModel<*, *, *, ParentId>, ParentId>(
    private val repository: IChildModelRepository<Model, *, *, *, ParentId>,
) : IListSliceChildModelUseCase<Model, ParentId> {

    override fun invoke(input1: Pagination, input2: ParentId): List<Model> = repository.list(input1, input2)

}
