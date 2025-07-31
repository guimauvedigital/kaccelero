package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IContext
import dev.kaccelero.models.IModel
import dev.kaccelero.repositories.IModelRepository

open class GetModelWithContextFromRepositoryUseCase<Model : IModel<Id, *, *>, Id>(
    repository: IModelRepository<Model, Id, *, *>,
) : GetChildModelWithContextFromRepositoryUseCase<Model, Id, Unit>(repository), IGetModelWithContextUseCase<Model, Id> {

    override fun invoke(input1: Id, input2: IContext): Model? = invoke(input1, Unit, input2)

    override fun invoke(input1: Id, input2: Unit, input3: IContext): Model? =
        super<GetChildModelWithContextFromRepositoryUseCase>.invoke(input1, input2, input3)

}
