package dev.kaccelero.commons.repositories

import dev.kaccelero.models.IModel
import dev.kaccelero.repositories.IModelRepository

open class UpdateModelFromRepositoryUseCase<Model : IModel<Id, *, UpdatePayload>, Id, UpdatePayload>(
    repository: IModelRepository<Model, Id, *, UpdatePayload>,
) : UpdateChildModelFromRepositoryUseCase<Model, Id, UpdatePayload, Unit>(repository),
    IUpdateModelUseCase<Model, Id, UpdatePayload> {

    override fun invoke(input1: Id, input2: UpdatePayload): Model? = invoke(input1, input2, Unit)

    override fun invoke(input1: Id, input2: UpdatePayload, input3: Unit): Model? =
        super<UpdateChildModelFromRepositoryUseCase>.invoke(input1, input2, input3)

}
