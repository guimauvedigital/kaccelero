package dev.kaccelero.models


interface IChildModel<Id, CreatePayload, UpdatePayload, ParentId> {

    val id: Id
    val parentId: ParentId

}
