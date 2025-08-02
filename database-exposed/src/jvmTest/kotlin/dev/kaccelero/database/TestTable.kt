package dev.kaccelero.database

import org.jetbrains.exposed.dao.id.UUIDTable

object TestTable : UUIDTable() {

    val name = varchar("name", 255)
    val externalId = uuid("external_id").index()
    val optionalId = uuid("optional_id").nullable()

}
