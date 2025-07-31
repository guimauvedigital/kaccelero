package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.repositories.IChildModelRepository

open class DeleteChildModelFromRepositoryUseCase<Model : IChildModel<Id, *, *, ParentId>, Id, ParentId>(
    private val repository: IChildModelRepository<Model, Id, *, *, ParentId>,
) : IDeleteChildModelUseCase<Model, Id, ParentId> {

    override fun invoke(input1: Id, input2: ParentId): Boolean = repository.delete(input1, input2)

}
