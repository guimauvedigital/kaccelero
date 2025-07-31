package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IChildModel
import dev.kaccelero.models.IContext
import dev.kaccelero.repositories.IChildModelRepository

open class CountChildModelWithContextFromRepositoryUseCase<Model : IChildModel<*, *, *, ParentId>, ParentId>(
    private val repository: IChildModelRepository<Model, *, *, *, ParentId>,
) : ICountChildModelWithContextUseCase<Model, ParentId> {

    override fun invoke(input1: ParentId, input2: IContext): Long = repository.count(input1, input2)

}
