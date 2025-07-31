package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IContext
import dev.kaccelero.models.IModel
import dev.kaccelero.repositories.IModelRepository

open class UpdateModelWithContextFromRepositoryUseCase<Model : IModel<Id, *, UpdatePayload>, Id, UpdatePayload>(
    repository: IModelRepository<Model, Id, *, UpdatePayload>,
) : UpdateChildModelWithContextFromRepositoryUseCase<Model, Id, UpdatePayload, Unit>(repository),
    IUpdateModelWithContextUseCase<Model, Id, UpdatePayload> {

    override fun invoke(input1: Id, input2: UpdatePayload, input3: IContext): Model? =
        invoke(input1, input2, Unit, input3)

    override fun invoke(input1: Id, input2: UpdatePayload, input3: Unit, input4: IContext): Model? =
        super<UpdateChildModelWithContextFromRepositoryUseCase>.invoke(input1, input2, input3, input4)

}
