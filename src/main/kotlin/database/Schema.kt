package io.github.stscoundrel.caravansary.database

import java.sql.Connection

object Schema {

    fun initialize(connection: Connection) {
        connection.createStatement().use { statement ->
            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS products (
                    source TEXT NOT NULL,
                    source_id TEXT NOT NULL,
                    id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    price NUMERIC NOT NULL,
                    date TEXT,
                    status TEXT NOT NULL,
                    last_seen_at TEXT,
                    PRIMARY KEY (source, source_id, id)
                )
                """.trimIndent()
            )
        }
    }
}