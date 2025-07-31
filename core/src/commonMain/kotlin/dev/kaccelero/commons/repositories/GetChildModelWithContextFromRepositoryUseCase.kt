package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.models.IContext
import dev.kaccelero.repositories.IChildModelRepository

open class GetChildModelWithContextFromRepositoryUseCase<Model : IChildModel<Id, *, *, ParentId>, Id, ParentId>(
    private val repository: IChildModelRepository<Model, Id, *, *, ParentId>,
) : IGetChildModelWithContextUseCase<Model, Id, ParentId> {

    override fun invoke(input1: Id, input2: ParentId, input3: IContext): Model? = repository.get(input1, input2, input3)

}
