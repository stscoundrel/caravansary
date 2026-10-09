package io.github.stscoundrel.caravansary

import io.github.stscoundrel.caravansary.application.CaravansaryService
import io.github.stscoundrel.caravansary.application.ProductTracker
import io.github.stscoundrel.caravansary.domain.*
import io.github.stscoundrel.caravansary.report.ProductReport
import io.github.stscoundrel.caravansary.report.ProductReportService
import java.math.BigDecimal
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class CaravansaryServiceTest {
    @Test
    fun `a failed scrape does not prevent later categories or report persistence`() {
        val savedProducts = mutableListOf<Product>()
        val markedSoldOut = mutableListOf<Product>()
        val products = object : ProductRepository {
            override fun findAll(source: ProductSource, sourceId: String) = listOf(
                Product(source, sourceId, "previous", "Previous", BigDecimal.ONE, null)
            )

            override fun saveAll(products: List<Product>) {
                savedProducts.addAll(products)
            }

            override fun markSoldOut(products: List<Product>) {
                markedSoldOut.addAll(products)
            }
        }
        var savedReport: ProductReport? = null
        val reports = object : ProductReportRepository {
            override fun save(report: ProductReport): Long {
                savedReport = report
                return 1L
            }

            override fun findById(id: Long) = savedReport
            override fun findLatest() = savedReport
            override fun findAll() = listOfNotNull(savedReport)
        }
        fun fetcher(category: String, fails: Boolean = false) = object : ProductFetcher {
            override val source = ProductSource.ASETALO
            override val sourceId = category
            override fun fetchProducts(): List<Product> {
                if (fails) error("Scrape failed")
                return listOf(
                    Product(source, sourceId, "current", "Current", BigDecimal.TEN, null)
                )
            }
        }

        val report = CaravansaryService(
            fetchers = listOf(fetcher("before"), fetcher("failed", true), fetcher("after")),
            tracker = ProductTracker(products),
            reportService = ProductReportService(),
            reportRepository = reports
        ).run()

        assertEquals(listOf("before", "after"), report.stores.map { it.sourceId })
        assertEquals(listOf("before", "after"), savedProducts.map { it.sourceId })
        assertEquals(listOf("before", "after"), markedSoldOut.map { it.sourceId })
        assertSame(report, savedReport)
        assertEquals(listOf(1, 1), report.stores.map { it.newProducts.size })
    }
}
