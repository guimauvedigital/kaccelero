# database-exposed

Definitions and extensions for Exposed databases.

## Installation

Add dependency to your `build.gradle(.kts)` or `pom.xml`:

```kotlin
api("dev.kaccelero:database-exposed:0.6.6")
```

```xml

<dependency>
    <groupId>dev.kaccelero</groupId>
    <artifactId>database-exposed-jvm</artifactId>
    <version>0.6.6</version>
</dependency>
```

## Usage

Define your database using the `IDatabase` interface:

```kotlin
class Database(
    protocol: String,
    host: String = "",
    name: String = "",
    user: String = "",
    password: String = "",
) : IDatabase {

    private val database: org.jetbrains.exposed.sql.Database = when (protocol) {
        "mysql" -> org.jetbrains.exposed.sql.Database.connect(
            "jdbc:mysql://$host:3306/$name", "com.mysql.cj.jdbc.Driver",
            user, password
        )

        "h2" -> org.jetbrains.exposed.sql.Database.connect(
            "jdbc:h2:mem:$name;DB_CLOSE_DELAY=-1;", "org.h2.Driver"
        )

        else -> throw Exception("Unsupported database protocol: $protocol")
    }

    override fun <T> transaction(statement: Transaction.() -> T): T = transaction(database, statement)

    override suspend fun <T> suspendedTransaction(statement: suspend Transaction.() -> T): T =
        newSuspendedTransaction(Dispatchers.IO, database) { statement() }

}
```

More coming soon...
