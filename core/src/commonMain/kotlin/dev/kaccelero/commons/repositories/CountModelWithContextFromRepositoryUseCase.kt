package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IContext
import dev.kaccelero.models.IModel
import dev.kaccelero.repositories.IModelRepository

open class CountModelWithContextFromRepositoryUseCase<Model : IModel<*, *, *>>(
    repository: IModelRepository<Model, *, *, *>,
) : CountChildModelWithContextFromRepositoryUseCase<Model, Unit>(repository), ICountModelWithContextUseCase<Model> {

    override fun invoke(input: IContext): Long = invoke(Unit, input)

    override fun invoke(input1: Unit, input2: IContext): Long =
        super<CountChildModelWithContextFromRepositoryUseCase>.invoke(input1, input2)

}
