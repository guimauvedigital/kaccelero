package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IContext
import dev.kaccelero.models.IModel
import dev.kaccelero.repositories.IModelRepository

open class ListModelWithContextFromRepositoryUseCase<Model : IModel<*, *, *>>(
    repository: IModelRepository<Model, *, *, *>,
) : ListChildModelWithContextFromRepositoryUseCase<Model, Unit>(repository), IListModelWithContextUseCase<Model> {

    override fun invoke(input: IContext): List<Model> = invoke(Unit, input)

    override fun invoke(input1: Unit, input2: IContext): List<Model> =
        super<ListChildModelWithContextFromRepositoryUseCase>.invoke(input1, input2)

}
