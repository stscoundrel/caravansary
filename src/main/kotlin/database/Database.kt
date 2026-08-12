package io.github.stscoundrel.caravansary.database

import java.sql.Connection
import java.sql.DriverManager

class Database(
    databasePath: String
) : AutoCloseable {

    val connection: Connection = DriverManager.getConnection(
        "jdbc:sqlite:$databasePath"
    )

    init {
        connection.createStatement().use { statement ->
            statement.execute("PRAGMA foreign_keys = ON")
        }

        Schema.initialize(connection)
    }

    override fun close() {
        connection.close()
    }
}