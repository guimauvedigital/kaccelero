package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IContext
import dev.kaccelero.models.IModel
import dev.kaccelero.repositories.IModelRepository

open class DeleteModelWithContextFromRepositoryUseCase<Model : IModel<Id, *, *>, Id>(
    repository: IModelRepository<Model, Id, *, *>,
) : DeleteChildModelWithContextFromRepositoryUseCase<Model, Id, Unit>(repository),
    IDeleteModelWithContextUseCase<Model, Id> {

    override fun invoke(input1: Id, input2: IContext): Boolean = invoke(input1, Unit, input2)

    override fun invoke(input1: Id, input2: Unit, input3: IContext): Boolean =
        super<DeleteChildModelWithContextFromRepositoryUseCase>.invoke(input1, input2, input3)

}
