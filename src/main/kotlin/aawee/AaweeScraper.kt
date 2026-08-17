package io.github.stscoundrel.caravansary.aawee

import com.microsoft.playwright.Browser
import com.microsoft.playwright.Locator
import com.microsoft.playwright.Playwright
import io.github.stscoundrel.caravansary.Product
import io.github.stscoundrel.caravansary.ProductFetcher
import io.github.stscoundrel.caravansary.ProductSource
import java.math.BigDecimal

class AaweeScraper(
    override val sourceId: String
) : ProductFetcher {

    companion object {
        private const val BASE_URL = "https://www.aawee.fi/fi"
    }

    override val source = ProductSource.AAWEE

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

                println("Found $productCount products")

                return (0 until productCount).mapNotNull { i ->
                    val product = products.nth(i)

                    if (product.textContent()?.contains("Tuote tilapäisesti loppu") == true) {
                        return@mapNotNull null
                    }

                    val productLink = product.locator(".ProductLink").first()

                    Product(
                        source = ProductSource.AAWEE,
                        sourceId = sourceId,
                        id = productLink.getAttribute("href") ?: "",
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

    private fun parsePrice(product: Locator): BigDecimal {
        val price = product.locator(".ProductPrice")

        val amount = price
            .locator(".Price--amount-wrapper")
            .first()
            .textContent()
            ?.trim()
            ?: throw IllegalArgumentException(
                "Product price is missing: ${price.textContent()}"
            )

        return parseAmount(amount)
    }

    private fun parseAmount(value: String): BigDecimal {
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
                "Unable to parse Aawee product price: '$value' " +
                        "(normalized: '$normalized')",
                e
            )
        }
    }
}