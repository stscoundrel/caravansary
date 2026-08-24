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

            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS scrape_runs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    generated_at TEXT NOT NULL
                )
                """.trimIndent()
            )

            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS scrape_results (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    scrape_run_id INTEGER NOT NULL,
                    source TEXT NOT NULL,
                    source_id TEXT NOT NULL,
                    current_count INTEGER NOT NULL,

                    FOREIGN KEY (scrape_run_id)
                        REFERENCES scrape_runs(id)
                )
                """.trimIndent()
            )

            statement.execute(
                """
                CREATE TABLE IF NOT EXISTS scrape_result_products (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    scrape_result_id INTEGER NOT NULL,
                    product_source TEXT NOT NULL,
                    product_source_id TEXT NOT NULL,
                    product_id TEXT NOT NULL,
                    status TEXT NOT NULL,
                    name TEXT NOT NULL,
                    price NUMERIC NOT NULL,
                    date TEXT,

                    FOREIGN KEY (scrape_result_id)
                        REFERENCES scrape_results(id)
                )
                """.trimIndent()
            )
        }
    }
}