package io.github.stscoundrel.caravansary.database

import io.github.stscoundrel.caravansary.Product
import io.github.stscoundrel.caravansary.ProductSource
import io.github.stscoundrel.caravansary.ProductStatus
import io.github.stscoundrel.caravansary.report.ProductReport
import io.github.stscoundrel.caravansary.report.ProductStoreReport
import io.github.stscoundrel.caravansary.ProductReportRepository
import java.sql.Connection
import java.sql.Statement
import java.time.LocalDate
import java.time.LocalDateTime

class SqliteProductReportRepository(
    private val connection: Connection
) : ProductReportRepository {

    override fun save(report: ProductReport): Long {
        val originalAutoCommit = connection.autoCommit

        connection.autoCommit = false

        try {
            val runId = insertScrapeRun(report)

            report.stores.forEach { store ->
                insertScrapeResult(
                    runId = runId,
                    store = store
                )
            }

            connection.commit()

            return runId
        } catch (e: Exception) {
            connection.rollback()
            throw e
        } finally {
            connection.autoCommit = originalAutoCommit
        }
    }

    override fun findById(id: Long): ProductReport? {
        val run = findScrapeRun(id) ?: return null
        val stores = findScrapeResults(id)

        return ProductReport(
            generatedAt = run.generatedAt,
            stores = stores
        )
    }

    override fun findLatest(): ProductReport? {
        val id = connection.prepareStatement(
            """
            SELECT id
            FROM scrape_runs
            ORDER BY generated_at DESC, id DESC
            LIMIT 1
            """.trimIndent()
        ).use { statement ->
            statement.executeQuery().use { resultSet ->
                if (!resultSet.next()) {
                    return null
                }

                resultSet.getLong("id")
            }
        }

        return findById(id)
    }

    override fun findAll(): List<ProductReport> {
        val ids = connection.prepareStatement(
            """
            SELECT id
            FROM scrape_runs
            ORDER BY generated_at DESC, id DESC
            """.trimIndent()
        ).use { statement ->
            statement.executeQuery().use { resultSet ->
                buildList {
                    while (resultSet.next()) {
                        add(resultSet.getLong("id"))
                    }
                }
            }
        }

        return ids.map { id ->
            findById(id)
                ?: error("Scrape run $id disappeared while reading reports")
        }
    }

    private fun insertScrapeRun(
        report: ProductReport
    ): Long {
        connection.prepareStatement(
            """
            INSERT INTO scrape_runs (generated_at)
            VALUES (?)
            """.trimIndent(),
            Statement.RETURN_GENERATED_KEYS
        ).use { statement ->
            statement.setString(
                1,
                report.generatedAt.toString()
            )

            statement.executeUpdate()

            statement.generatedKeys.use { keys ->
                if (!keys.next()) {
                    error("Unable to retrieve generated scrape run ID")
                }

                return keys.getLong(1)
            }
        }
    }

    private fun insertScrapeResult(
        runId: Long,
        store: ProductStoreReport
    ) {
        val resultId = connection.prepareStatement(
            """
            INSERT INTO scrape_results (
                scrape_run_id,
                source,
                source_id,
                current_count
            )
            VALUES (?, ?, ?, ?)
            """.trimIndent(),
            Statement.RETURN_GENERATED_KEYS
        ).use { statement ->
            statement.setLong(1, runId)
            statement.setString(2, store.source.name)
            statement.setString(3, store.sourceId)
            statement.setInt(4, store.currentCount)

            statement.executeUpdate()

            statement.generatedKeys.use { keys ->
                if (!keys.next()) {
                    error("Unable to retrieve generated scrape result ID")
                }

                keys.getLong(1)
            }
        }

        store.newProducts.forEach { product ->
            insertProductSnapshot(
                resultId = resultId,
                product = product,
                status = ProductStatus.ACTIVE
            )
        }

        store.soldOutProducts.forEach { product ->
            insertProductSnapshot(
                resultId = resultId,
                product = product,
                status = ProductStatus.SOLD_OUT
            )
        }
    }

    private fun insertProductSnapshot(
        resultId: Long,
        product: Product,
        status: ProductStatus
    ) {
        connection.prepareStatement(
            """
            INSERT INTO scrape_result_products (
                scrape_result_id,
                product_source,
                product_source_id,
                product_id,
                status,
                name,
                price,
                date
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
        ).use { statement ->
            statement.setLong(1, resultId)
            statement.setString(2, product.source.name)
            statement.setString(3, product.sourceId)
            statement.setString(4, product.id)
            statement.setString(5, status.name)
            statement.setString(6, product.name)
            statement.setBigDecimal(7, product.price)

            if (product.date != null) {
                statement.setString(8, product.date.toString())
            } else {
                statement.setNull(
                    8,
                    java.sql.Types.VARCHAR
                )
            }

            statement.executeUpdate()
        }
    }

    private fun findScrapeRun(
        id: Long
    ): ScrapeRunRow? {
        connection.prepareStatement(
            """
            SELECT id, generated_at
            FROM scrape_runs
            WHERE id = ?
            """.trimIndent()
        ).use { statement ->
            statement.setLong(1, id)

            statement.executeQuery().use { resultSet ->
                if (!resultSet.next()) {
                    return null
                }

                return ScrapeRunRow(
                    id = resultSet.getLong("id"),
                    generatedAt = LocalDateTime.parse(
                        resultSet.getString("generated_at")
                    )
                )
            }
        }
    }

    private fun findScrapeResults(
        runId: Long
    ): List<ProductStoreReport> {
        connection.prepareStatement(
            """
            SELECT
                id,
                source,
                source_id,
                current_count
            FROM scrape_results
            WHERE scrape_run_id = ?
            ORDER BY id
            """.trimIndent()
        ).use { statement ->
            statement.setLong(1, runId)

            statement.executeQuery().use { resultSet ->
                return buildList {
                    while (resultSet.next()) {
                        val resultId = resultSet.getLong("id")

                        add(
                            loadStoreReport(
                                resultId = resultId,
                                source = ProductSource.valueOf(
                                    resultSet.getString("source")
                                ),
                                sourceId = resultSet.getString("source_id"),
                                currentCount = resultSet.getInt(
                                    "current_count"
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    private fun loadStoreReport(
        resultId: Long,
        source: ProductSource,
        sourceId: String,
        currentCount: Int
    ): ProductStoreReport {
        val snapshots = findProductSnapshots(resultId)

        return ProductStoreReport(
            source = source,
            sourceId = sourceId,
            currentCount = currentCount,
            newProducts = snapshots
                .filter { it.status == ProductStatus.ACTIVE }
                .map { it.product },
            soldOutProducts = snapshots
                .filter { it.status == ProductStatus.SOLD_OUT }
                .map { it.product }
        )
    }

    private fun findProductSnapshots(
        resultId: Long
    ): List<ProductSnapshot> {
        connection.prepareStatement(
            """
            SELECT
                product_source,
                product_source_id,
                product_id,
                status,
                name,
                price,
                date
            FROM scrape_result_products
            WHERE scrape_result_id = ?
            ORDER BY id
            """.trimIndent()
        ).use { statement ->
            statement.setLong(1, resultId)

            statement.executeQuery().use { resultSet ->
                return buildList {
                    while (resultSet.next()) {
                        val status = ProductStatus.valueOf(
                            resultSet.getString("status")
                        )

                        add(
                            ProductSnapshot(
                                status = status,
                                product = Product(
                                    source = ProductSource.valueOf(
                                        resultSet.getString(
                                            "product_source"
                                        )
                                    ),
                                    sourceId = resultSet.getString(
                                        "product_source_id"
                                    ),
                                    id = resultSet.getString("product_id"),
                                    name = resultSet.getString("name"),
                                    price = resultSet.getBigDecimal("price"),
                                    date = resultSet.getString("date")
                                        ?.let(LocalDate::parse),
                                    status = status,
                                    lastSeenAt = null
                                )
                            )
                        )
                    }
                }
            }
        }
    }

    private data class ScrapeRunRow(
        val id: Long,
        val generatedAt: LocalDateTime
    )

    private data class ProductSnapshot(
        val status: ProductStatus,
        val product: Product
    )
}