package dev.kaccelero.database

import dev.kaccelero.models.UUID
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNotNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.isNull
import org.jetbrains.exposed.sql.SqlExpressionBuilder.wrap
import org.jetbrains.exposed.sql.ops.InListOrNotInListBaseOp
import org.jetbrains.exposed.sql.ops.SingleValueInListOp
import org.jetbrains.exposed.sql.statements.UpdateBuilder

// MARK: - Entity ID table

@Suppress("UNCHECKED_CAST")
private val IColumnType<EntityID<java.util.UUID>>.table
    get() = (this as EntityIDColumnType<*>).idColumn.table as IdTable<java.util.UUID>

// MARK: - eq

infix fun ExpressionWithColumnType<java.util.UUID>.eq(t: UUID): Op<Boolean> =
    EqOp(this, wrap(t.javaUUID))

@JvmName("eqEntityID")
infix fun ExpressionWithColumnType<EntityID<java.util.UUID>>.eq(t: UUID): Op<Boolean> =
    EqOp(this, wrap(EntityID(t.javaUUID, columnType.table)))

@JvmName("eqNullableID")
infix fun ExpressionWithColumnType<java.util.UUID?>.eq(t: UUID?): Op<Boolean> = when (t) {
    null -> isNull()
    else -> EqOp(this, wrap(t.javaUUID))
}

// MARK: - neq

infix fun ExpressionWithColumnType<java.util.UUID>.neq(t: UUID): Op<Boolean> =
    NeqOp(this, wrap(t.javaUUID))

@JvmName("neqEntityID")
infix fun ExpressionWithColumnType<EntityID<java.util.UUID>>.neq(t: UUID): Op<Boolean> =
    NeqOp(this, wrap(EntityID(t.javaUUID, columnType.table)))

@JvmName("neqNullableID")
infix fun ExpressionWithColumnType<java.util.UUID?>.neq(t: UUID?): Op<Boolean> = when (t) {
    null -> isNotNull()
    else -> NeqOp(this, wrap(t.javaUUID))
}

// MARK: - inList

infix fun ExpressionWithColumnType<java.util.UUID>.inList(list: Iterable<UUID>): InListOrNotInListBaseOp<java.util.UUID> =
    SingleValueInListOp(this, list.map { it.javaUUID }, isInList = true)

@JvmName("inListEntityID")
infix fun ExpressionWithColumnType<EntityID<java.util.UUID>>.inList(list: Iterable<UUID>): InListOrNotInListBaseOp<EntityID<java.util.UUID>> =
    SingleValueInListOp(this, list.map { EntityID(it.javaUUID, columnType.table) }, isInList = true)

@JvmName("inListNullableID")
infix fun ExpressionWithColumnType<java.util.UUID?>.inList(list: Iterable<UUID>): InListOrNotInListBaseOp<java.util.UUID?> =
    SingleValueInListOp(this, list.map { it.javaUUID }, isInList = true)

// MARK: - notInList

infix fun ExpressionWithColumnType<java.util.UUID>.notInList(list: Iterable<UUID>): InListOrNotInListBaseOp<java.util.UUID> =
    SingleValueInListOp(this, list.map { it.javaUUID }, isInList = false)

@JvmName("notInListEntityID")
infix fun ExpressionWithColumnType<EntityID<java.util.UUID>>.notInList(list: Iterable<UUID>): InListOrNotInListBaseOp<EntityID<java.util.UUID>> =
    SingleValueInListOp(this, list.map { EntityID(it.javaUUID, columnType.table) }, isInList = false)

@JvmName("notInListNullableID")
infix fun ExpressionWithColumnType<java.util.UUID?>.notInList(list: Iterable<UUID>): InListOrNotInListBaseOp<java.util.UUID?> =
    SingleValueInListOp(this, list.map { it.javaUUID }, isInList = false)

// MARK: - set

operator fun <T> UpdateBuilder<T>.set(column: Column<java.util.UUID>, value: UUID) =
    set(column, value.javaUUID)

@JvmName("setNullableID")
operator fun <T> UpdateBuilder<T>.set(column: Column<java.util.UUID?>, value: UUID?) =
    set(column, value?.javaUUID)
