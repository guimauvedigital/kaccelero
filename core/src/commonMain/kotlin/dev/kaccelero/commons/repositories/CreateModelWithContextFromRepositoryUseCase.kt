package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IContext
import dev.kaccelero.models.IModel
import dev.kaccelero.repositories.IModelRepository

open class CreateModelWithContextFromRepositoryUseCase<Model : IModel<*, CreatePayload, *>, CreatePayload>(
    repository: IModelRepository<Model, *, CreatePayload, *>,
) : CreateChildModelWithContextFromRepositoryUseCase<Model, CreatePayload, Unit>(repository),
    ICreateModelWithContextUseCase<Model, CreatePayload> {

    override fun invoke(input1: CreatePayload, input2: IContext): Model? = invoke(input1, Unit, input2)

    override fun invoke(input1: CreatePayload, input2: Unit, input3: IContext): Model? =
        super<CreateChildModelWithContextFromRepositoryUseCase>.invoke(input1, input2, input3)

}
