package dev.kaccelero.database

import dev.kaccelero.models.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

class UUIDExtensionTest {

    @Test
    fun testEq() {
        val database = Database("testEq")
        val uuid = UUID()
        assertEquals(
            "TEST.ID = '$uuid'",
            database.transaction { (TestTable.id eq uuid).toString() }
        )
        assertEquals(
            "TEST.EXTERNAL_ID = '$uuid'",
            database.transaction { (TestTable.externalId eq uuid).toString() }
        )
        assertEquals(
            "TEST.OPTIONAL_ID = '$uuid'",
            database.transaction { (TestTable.optionalId eq uuid).toString() }
        )
        assertEquals(
            "TEST.OPTIONAL_ID IS NULL",
            database.transaction { (TestTable.optionalId eq null).toString() }
        )
    }

    @Test
    fun testNeq() {
        val database = Database("testNeq")
        val uuid = UUID()
        assertEquals(
            "TEST.ID <> '$uuid'",
            database.transaction { (TestTable.id neq uuid).toString() }
        )
        assertEquals(
            "TEST.EXTERNAL_ID <> '$uuid'",
            database.transaction { (TestTable.externalId neq uuid).toString() }
        )
        assertEquals(
            "TEST.OPTIONAL_ID <> '$uuid'",
            database.transaction { (TestTable.optionalId neq uuid).toString() }
        )
        assertEquals(
            "TEST.OPTIONAL_ID IS NOT NULL",
            database.transaction { (TestTable.optionalId neq null).toString() }
        )
    }

}
