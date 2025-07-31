package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.repositories.IChildModelRepository

open class GetChildModelFromRepositoryUseCase<Model : IChildModel<Id, *, *, ParentId>, Id, ParentId>(
    private val repository: IChildModelRepository<Model, Id, *, *, ParentId>,
) : IGetChildModelUseCase<Model, Id, ParentId> {

    override fun invoke(input1: Id, input2: ParentId): Model? = repository.get(input1, input2)

}
