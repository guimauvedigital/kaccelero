package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.repositories.IChildModelRepository

open class CreateChildModelFromRepositoryUseCase<Model : IChildModel<*, CreatePayload, *, ParentId>, CreatePayload, ParentId>(
    private val repository: IChildModelRepository<Model, *, CreatePayload, *, ParentId>,
) : ICreateChildModelUseCase<Model, CreatePayload, ParentId> {

    override fun invoke(input1: CreatePayload, input2: ParentId): Model? = repository.create(input1, input2)

}
