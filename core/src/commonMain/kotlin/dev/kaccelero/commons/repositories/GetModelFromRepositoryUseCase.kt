package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IModel
import dev.kaccelero.repositories.IModelRepository

open class GetModelFromRepositoryUseCase<Model : IModel<Id, *, *>, Id>(
    repository: IModelRepository<Model, Id, *, *>,
) : GetChildModelFromRepositoryUseCase<Model, Id, Unit>(repository), IGetModelUseCase<Model, Id> {

    override fun invoke(input: Id): Model? = invoke(input, Unit)

    override fun invoke(input1: Id, input2: Unit): Model? =
        super<GetChildModelFromRepositoryUseCase>.invoke(input1, input2)

}
