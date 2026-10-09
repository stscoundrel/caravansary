package io.github.stscoundrel.caravansary.scraper.asenurkka

import com.microsoft.playwright.Playwright
import io.github.stscoundrel.caravansary.domain.Product
import io.github.stscoundrel.caravansary.domain.ProductFetcher
import io.github.stscoundrel.caravansary.scraper.ScraperConfig
import io.github.stscoundrel.caravansary.domain.ProductSource
import java.math.BigDecimal

class AsenurkkaScraper(
    override val sourceId: String
) : ProductFetcher {

    companion object {
        private const val BASE_URL = "https://asenurkka.fi"
    }

    override val source = ProductSource.ASENURKKA

    override fun fetchProducts(): List<Product> {
        val url = "$BASE_URL$sourceId"

        Playwright.create().use { playwright ->
            playwright.chromium().launch().use { browser ->
                val page = browser.newPage(ScraperConfig.pageOptions())

                page.navigate(url)

                page.locator("ul.products").waitFor()

                val products = page.locator("ul.products li.product")
                val productCount = products.count()

                return (0 until productCount).map { i ->
                    val product = products.nth(i)

                    val productLink = product
                        .locator("a")
                        .first()

                    Product(
                        source = ProductSource.ASENURKKA,
                        sourceId = sourceId,
                        id = productLink
                            .getAttribute("href")
                            ?: "",
                        name = product
                            .locator(".product-title")
                            .textContent()
                            ?.trim() ?: "",
                        price = parsePrice(product),
                        date = null
                    )
                }
            }
        }
    }

    private fun parsePrice(
        product: com.microsoft.playwright.Locator
    ): BigDecimal {
        val discountedPrice = product.locator(
            "ins .woocommerce-Price-amount"
        )

        val regularPrice = product.locator(
            ".woocommerce-Price-amount"
        )

        val value = when {
            discountedPrice.count() > 0 ->
                discountedPrice.first().textContent()

            regularPrice.count() > 0 ->
                regularPrice.first().textContent()

            else ->
                null
        }

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
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException(
                "Unable to parse Asenurkka product price: '$value' " +
                        "(normalized: '$normalized')",
                e
            )
        }
    }
}