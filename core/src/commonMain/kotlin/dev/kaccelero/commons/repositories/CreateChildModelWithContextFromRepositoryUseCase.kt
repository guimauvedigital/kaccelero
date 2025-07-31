package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.models.IContext
import dev.kaccelero.repositories.IChildModelRepository

open class CreateChildModelWithContextFromRepositoryUseCase<Model : IChildModel<*, CreatePayload, *, ParentId>, CreatePayload, ParentId>(
    private val repository: IChildModelRepository<Model, *, CreatePayload, *, ParentId>,
) : ICreateChildModelWithContextUseCase<Model, CreatePayload, ParentId> {

    override fun invoke(input1: CreatePayload, input2: ParentId, input3: IContext): Model? =
        repository.create(input1, input2, input3)

}
