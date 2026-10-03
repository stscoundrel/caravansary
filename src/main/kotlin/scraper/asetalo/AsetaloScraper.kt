package io.github.stscoundrel.caravansary.scraper.asetalo

import com.microsoft.playwright.Playwright
import io.github.stscoundrel.caravansary.domain.Product
import io.github.stscoundrel.caravansary.domain.ProductFetcher
import io.github.stscoundrel.caravansary.domain.ProductSource
import io.github.stscoundrel.caravansary.scraper.newScraperContext
import java.math.BigDecimal
import java.time.LocalDate

class AsetaloScraper(override val sourceId: String) : ProductFetcher {
    override val source = ProductSource.ASETALO

    companion object {
        private const val BASE_URL = "https://asetalo.fi"
    }

    override fun fetchProducts(): List<Product> {
        val url = "$BASE_URL$sourceId"

        Playwright.create().use { playwright ->
            playwright.chromium().launch().use { browser ->
                val context = browser.newScraperContext()
                val page = context.newPage()

                page.navigate(url)

                val products = page.locator(".tuotelistauskortti")
                val productCount = products.count()

                return (0 until productCount).map { i ->
                    val product = products.nth(i)

                    Product(
                        source = ProductSource.ASETALO,
                        sourceId = sourceId,
                        id = product.getAttribute("id") ?: "",
                        name = product
                            .locator(".selaus_tuotenimi_iso")
                            .textContent()
                            ?.trim() ?: "",
                        price = parsePrice(
                            product
                                .locator(".selaus_tuotehinta")
                                .textContent()
                        ),
                        date = product
                            .getAttribute("data-e")
                            ?.takeIf { it.isNotBlank() }
                            ?.let { LocalDate.parse(it) }
                    )
                }
            }
        }
    }

    private fun parsePrice(value: String?): BigDecimal {
        require(!value.isNullOrBlank()) {
            "Product price is missing"
        }

        val normalized = value
            .replace("\u00A0", "")
            .replace(" ", "")
            .replace("€", "")
            .replace(",", ".")
            .trim()

        return try {
            BigDecimal(normalized)
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException(
                "Unable to parse product price: '$value' (normalized: '$normalized')",
                e
            )
        }
    }
}
