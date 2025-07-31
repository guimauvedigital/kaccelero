package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IModel
import dev.kaccelero.repositories.IModelRepository

open class ListModelFromRepositoryUseCase<Model : IModel<*, *, *>>(
    repository: IModelRepository<Model, *, *, *>,
) : ListChildModelFromRepositoryUseCase<Model, Unit>(repository), IListModelUseCase<Model> {

    override fun invoke(): List<Model> = invoke(Unit)

    override fun invoke(input: Unit): List<Model> = super<ListChildModelFromRepositoryUseCase>.invoke(input)

}
