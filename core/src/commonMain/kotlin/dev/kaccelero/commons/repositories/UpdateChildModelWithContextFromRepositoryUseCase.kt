package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.models.IContext
import dev.kaccelero.repositories.IChildModelRepository

open class UpdateChildModelWithContextFromRepositoryUseCase<Model : IChildModel<Id, *, UpdatePayload, ParentId>, Id, UpdatePayload, ParentId>(
    private val repository: IChildModelRepository<Model, Id, *, UpdatePayload, ParentId>,
) : IUpdateChildModelWithContextUseCase<Model, Id, UpdatePayload, ParentId> {

    override fun invoke(input1: Id, input2: UpdatePayload, input3: ParentId, input4: IContext): Model? =
        if (repository.update(input1, input2, input3, input4)) repository.get(input1, input3, input4)
        else null

}
