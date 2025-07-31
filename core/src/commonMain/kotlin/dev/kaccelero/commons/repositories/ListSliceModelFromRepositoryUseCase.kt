package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IModel
import dev.kaccelero.repositories.IModelRepository
import dev.kaccelero.repositories.Pagination

open class ListSliceModelFromRepositoryUseCase<Model : IModel<*, *, *>>(
    repository: IModelRepository<Model, *, *, *>,
) : ListSliceChildModelFromRepositoryUseCase<Model, Unit>(repository), IListSliceModelUseCase<Model> {

    override fun invoke(input: Pagination): List<Model> = invoke(input, Unit)

    override fun invoke(input1: Pagination, input2: Unit): List<Model> =
        super<ListSliceChildModelFromRepositoryUseCase>.invoke(input1, input2)

}
