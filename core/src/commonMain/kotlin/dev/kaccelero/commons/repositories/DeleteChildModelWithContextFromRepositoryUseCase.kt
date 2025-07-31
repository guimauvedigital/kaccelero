package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.models.IContext
import dev.kaccelero.repositories.IChildModelRepository

open class DeleteChildModelWithContextFromRepositoryUseCase<Model : IChildModel<Id, *, *, ParentId>, Id, ParentId>(
    private val repository: IChildModelRepository<Model, Id, *, *, ParentId>,
) : IDeleteChildModelWithContextUseCase<Model, Id, ParentId> {

    override fun invoke(input1: Id, input2: ParentId, input3: IContext): Boolean =
        repository.delete(input1, input2, input3)

}
