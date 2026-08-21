package io.github.stscoundrel.caravansary.fusil

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.github.stscoundrel.caravansary.Product
import io.github.stscoundrel.caravansary.ProductFetcher
import io.github.stscoundrel.caravansary.ProductSource
import java.math.BigDecimal
import java.time.LocalDate

class FusilScraper(
    override val sourceId: String
) : ProductFetcher {

    companion object {
        private const val BASE_URL = "https://fusil.fi"
    }

    override val source = ProductSource.FUSIL

    override fun fetchProducts(): List<Product> {
        val url = "$BASE_URL$sourceId"

        Playwright.create().use { playwright ->
            playwright.chromium().launch().use { browser ->
                val page = browser.newPage()

                page.navigate(url)

                val pageCount = getPageCount(page)

                return (1..pageCount).flatMap { pageNumber ->
                    fetchPage(
                        page = page,
                        pageNumber = pageNumber
                    )
                }
            }
        }
    }

    private fun getPageCount(page: Page): Int {
        val pagination = page.locator(".nav2 .page").first()

        val text = pagination.textContent()?.trim()
            ?: throw IllegalStateException("Pagination page count not found")

        val match = Regex("""Sivu\s+\d+/(\d+)""").find(text)
            ?: throw IllegalStateException(
                "Unable to parse Fusil page count from: '$text'"
            )

        return match.groupValues[1].toInt()
    }

    private fun fetchPage(
        page: Page,
        pageNumber: Int
    ): List<Product> {
        val url = "$BASE_URL$sourceId&page=$pageNumber"

        page.navigate(url)

        val products = page.locator(".item")
        val productCount = products.count()

        return (0 until productCount).map { i ->
            val product = products.nth(i)

            Product(
                source = ProductSource.FUSIL,
                sourceId = sourceId,
                id = parseProductId(product),
                name = product
                    .locator(".itemname")
                    .textContent()
                    ?.trim() ?: "",
                price = parsePrice(
                    product
                        .locator(".itemprice")
                        .textContent()
                ),
                date = parseDate(product)
            )
        }
    }

    private fun parseProductId(product: Locator): String {
        val itemInfo = product
            .locator(".iteminfo2")
            .textContent()
            ?: return ""

        val match = Regex(
            """TUOTE\s*NRO\s*(\d+)"""
        ).find(itemInfo.replace(Regex("""\s+"""), " "))

        return match?.groupValues?.get(1) ?: ""
    }

    private fun parseDate(product: Locator): LocalDate? {
        val itemInfo = product
            .locator(".iteminfo2")
            .textContent()
            ?: return null

        val match = Regex(
            """LISÄTTY\s+(\d{4}-\d{2}-\d{2})"""
        ).find(itemInfo.replace(Regex("""\s+"""), " "))

        return match
            ?.groupValues
            ?.get(1)
            ?.let(LocalDate::parse)
    }

    private fun parsePrice(value: String?): BigDecimal {
        if (value.isNullOrBlank()) {
            return BigDecimal.ZERO
        }

        val normalized = value
            .replace("\u00A0", "")
            .replace(" ", "")
            .replace("€", "")
            .replace(",", ".")
            .trim()

        return try {
            BigDecimal(normalized)
        } catch (_: NumberFormatException) {
            BigDecimal.ZERO
        }
    }
}