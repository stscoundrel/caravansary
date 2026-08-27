package io.github.stscoundrel.caravansary.erakala

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Playwright
import io.github.stscoundrel.caravansary.domain.Product
import io.github.stscoundrel.caravansary.domain.ProductFetcher
import io.github.stscoundrel.caravansary.domain.ProductSource
import java.math.BigDecimal

class ErakalaScraper(
    override val sourceId: String
) : ProductFetcher {

    companion object {
        private const val BASE_URL = "https://www.erakala.fi"
    }

    override val source = ProductSource.ERAKALA

    override fun fetchProducts(): List<Product> {
        val url = "$BASE_URL$sourceId"

        Playwright.create().use { playwright ->
            playwright.chromium().launch().use { browser ->
                val context = browser.newContext(
                    Browser.NewContextOptions()
                        .setUserAgent(
                            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) " +
                                    "AppleWebKit/537.36 (KHTML, like Gecko) " +
                                    "Chrome/139.0.0.0 Safari/537.36"
                        )
                )

                val page = context.newPage()
                page.navigate(url)

                page.locator(".ProductList").waitFor()

                val products = page.locator(".ProductCard")
                val productCount = products.count()

                return (0 until productCount).map { i ->
                    val product = products.nth(i)

                    val productLink = product
                        .locator(".ProductLink")
                        .first()

                    Product(
                        source = ProductSource.ERAKALA,
                        sourceId = sourceId,
                        id = productLink
                            .getAttribute("href")
                            ?: "",
                        name = product
                            .locator(".ProductLink h2")
                            .textContent()
                            ?.trim() ?: "",
                        price = parsePrice(product),
                        date = null
                    )
                }
            }
        }
    }

    private fun parsePrice(product: com.microsoft.playwright.Locator): BigDecimal {
        val price = product.locator(
            ".Price--amount-wrapper"
        ).first()

        val value = price.textContent()

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
                "Unable to parse Eräkala product price: '$value' " +
                        "(normalized: '$normalized')",
                e
            )
        }
    }
}