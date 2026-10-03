package io.github.stscoundrel.caravansary.scraper.pphunt

import com.microsoft.playwright.Playwright
import io.github.stscoundrel.caravansary.domain.Product
import io.github.stscoundrel.caravansary.domain.ProductFetcher
import io.github.stscoundrel.caravansary.domain.ProductSource
import io.github.stscoundrel.caravansary.scraper.newScraperContext
import java.math.BigDecimal

class PpHuntScraper(
    override val sourceId: String
) : ProductFetcher {

    companion object {
        private const val BASE_URL = "https://www.pphunt.fi"
    }

    override val source = ProductSource.PP_HUNT

    override fun fetchProducts(): List<Product> {
        val url = "$BASE_URL$sourceId"

        Playwright.create().use { playwright ->
            playwright.chromium().launch().use { browser ->
                val context = browser.newScraperContext()
                val page = context.newPage()

                page.navigate(url)

                val products = page.locator("article.item")
                val productCount = products.count()

                return (0 until productCount).map { i ->
                    val product = products.nth(i)
                    val productLink = product.locator("a").first()

                    Product(
                        source = ProductSource.PP_HUNT,
                        sourceId = sourceId,
                        id = productLink.getAttribute("href") ?: "",
                        name = product
                            .locator(".item-title")
                            .textContent()
                            ?.trim() ?: "",
                        price = parsePrice(
                            product
                                .locator(".item-price .wnd-product-price")
                                .textContent()
                        ),
                        date = null
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
            .replace(",", ".")
            .trim()

        return try {
            BigDecimal(normalized)
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException(
                "Unable to parse PP Hunt product price: '$value' " +
                        "(normalized: '$normalized')",
                e
            )
        }
    }
}
