package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IContext
import dev.kaccelero.models.IModel
import dev.kaccelero.repositories.IModelRepository
import dev.kaccelero.repositories.Pagination

open class ListSliceModelWithContextFromRepositoryUseCase<Model : IModel<*, *, *>>(
    repository: IModelRepository<Model, *, *, *>,
) : ListSliceChildModelWithContextFromRepositoryUseCase<Model, Unit>(repository),
    IListSliceModelWithContextUseCase<Model> {

    override fun invoke(input1: Pagination, input2: IContext): List<Model> =
        invoke(input1, Unit, input2)

    override fun invoke(input1: Pagination, input2: Unit, input3: IContext): List<Model> =
        super<ListSliceChildModelWithContextFromRepositoryUseCase>.invoke(input1, input2, input3)

}
