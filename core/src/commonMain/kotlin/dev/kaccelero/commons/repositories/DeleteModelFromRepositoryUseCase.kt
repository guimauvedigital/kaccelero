package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IModel
import dev.kaccelero.repositories.IModelRepository

open class DeleteModelFromRepositoryUseCase<Model : IModel<Id, *, *>, Id>(
    repository: IModelRepository<Model, Id, *, *>,
) : DeleteChildModelFromRepositoryUseCase<Model, Id, Unit>(repository), IDeleteModelUseCase<Model, Id> {

    override fun invoke(input: Id): Boolean = invoke(input, Unit)

    override fun invoke(input1: Id, input2: Unit): Boolean =
        super<DeleteChildModelFromRepositoryUseCase>.invoke(input1, input2)

}
