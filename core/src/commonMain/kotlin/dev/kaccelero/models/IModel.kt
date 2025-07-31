package dev.kaccelero.models


interface IModel<Id, CreatePayload, UpdatePayload> : IChildModel<Id, CreatePayload, UpdatePayload, Unit> {

    override val id: Id

    override val parentId: Unit
        get() = Unit

}
