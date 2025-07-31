package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IModel
import dev.kaccelero.repositories.IModelRepository

open class CountModelFromRepositoryUseCase<Model : IModel<*, *, *>>(
    repository: IModelRepository<Model, *, *, *>,
) : CountChildModelFromRepositoryUseCase<Model, Unit>(repository), ICountModelUseCase<Model> {

    override fun invoke(): Long = invoke(Unit)

    override fun invoke(input: Unit): Long = super<CountChildModelFromRepositoryUseCase>.invoke(input)

}
