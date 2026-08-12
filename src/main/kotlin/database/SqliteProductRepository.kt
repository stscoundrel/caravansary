package io.github.stscoundrel.caravansary.database

import io.github.stscoundrel.caravansary.Product
import io.github.stscoundrel.caravansary.ProductRepository
import io.github.stscoundrel.caravansary.ProductSource
import io.github.stscoundrel.caravansary.ProductStatus
import java.sql.Connection
import java.time.Instant
import java.time.LocalDate

class SqliteProductRepository(
    private val connection: Connection
) : ProductRepository {

    override fun saveAll(products: List<Product>) {
        val lastSeenAt = Instant.now()

        connection.autoCommit = false

        try {
            products.forEach { product ->
                save(product, lastSeenAt)
            }

            connection.commit()
        } catch (exception: Exception) {
            connection.rollback()
            throw exception
        } finally {
            connection.autoCommit = true
        }
    }

    private fun save(
        product: Product,
        lastSeenAt: Instant
    ) {
        connection.prepareStatement(
            """
            INSERT INTO products (
                source,
                source_id,
                id,
                name,
                price,
                date,
                status,
                last_seen_at
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(source, source_id, id)
            DO UPDATE SET
                name = excluded.name,
                price = excluded.price,
                date = excluded.date,
                status = excluded.status,
                last_seen_at = excluded.last_seen_at
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, product.source.name)
            statement.setString(2, product.sourceId)
            statement.setString(3, product.id)
            statement.setString(4, product.name)
            statement.setBigDecimal(5, product.price)
            statement.setString(6, product.date?.toString())
            statement.setString(7, ProductStatus.ACTIVE.name)
            statement.setString(8, lastSeenAt.toString())

            statement.executeUpdate()
        }
    }

    override fun findAll(
        source: ProductSource,
        sourceId: String
    ): List<Product> {
        connection.prepareStatement(
            """
            SELECT
                source,
                source_id,
                id,
                name,
                price,
                date,
                status,
                last_seen_at
            FROM products
            WHERE source = ?
              AND source_id = ?
            """.trimIndent()
        ).use { statement ->
            statement.setString(1, source.name)
            statement.setString(2, sourceId)

            statement.executeQuery().use { resultSet ->
                val products = mutableListOf<Product>()

                while (resultSet.next()) {
                    products += Product(
                        source = ProductSource.valueOf(
                            resultSet.getString("source")
                        ),
                        sourceId = resultSet.getString("source_id"),
                        id = resultSet.getString("id"),
                        name = resultSet.getString("name"),
                        price = resultSet.getBigDecimal("price"),
                        date = resultSet.getString("date")
                            ?.takeIf { it.isNotBlank() }
                            ?.let(LocalDate::parse),
                        status = ProductStatus.valueOf(
                            resultSet.getString("status")
                        ),
                        lastSeenAt = resultSet.getString("last_seen_at")
                            ?.takeIf { it.isNotBlank() }
                            ?.let(Instant::parse)
                    )
                }

                return products
            }
        }
    }

    override fun markSoldOut(products: List<Product>) {
        connection.prepareStatement(
            """
        UPDATE products
        SET status = ?
        WHERE source = ?
          AND source_id = ?
          AND id = ?
        """.trimIndent()
        ).use { statement ->
            connection.autoCommit = false

            try {
                products.forEach { product ->
                    statement.setString(1, ProductStatus.SOLD_OUT.name)
                    statement.setString(2, product.source.name)
                    statement.setString(3, product.sourceId)
                    statement.setString(4, product.id)
                    statement.addBatch()
                }

                statement.executeBatch()
                connection.commit()
            } catch (exception: Exception) {
                connection.rollback()
                throw exception
            } finally {
                connection.autoCommit = true
            }
        }
    }
}