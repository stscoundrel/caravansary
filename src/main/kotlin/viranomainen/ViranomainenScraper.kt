package io.github.stscoundrel.caravansary.viranomainen

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Playwright
import io.github.stscoundrel.caravansary.Product
import io.github.stscoundrel.caravansary.ProductFetcher
import io.github.stscoundrel.caravansary.ProductSource
import java.math.BigDecimal

class ViranomainenScraper(
    override val sourceId: String
) : ProductFetcher {

    companion object {
        private const val BASE_URL = "https://viranomainen.fi"
    }

    override val source = ProductSource.VIRANOMAINEN

    override fun fetchProducts(): List<Product> {
        val url = "$BASE_URL$sourceId"

        Playwright.create().use { playwright ->
            playwright.chromium().launch().use { browser ->
                val page = browser.newPage()

                page.navigate(url)

                val products = page.locator(
                    "#tuotelistaus_div a.ajaxlinkki.item"
                )

                val productCount = products.count()

                return (0 until productCount).map { i ->
                    val product = products.nth(i)

                    Product(
                        source = ProductSource.VIRANOMAINEN,
                        sourceId = sourceId,
                        id = product.getAttribute("href") ?: "",
                        name = product
                            .locator(".item_name")
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
        val priceElement = product.locator(
            "div[style*='color:black'] > div"
        ).first()

        val value = priceElement.evaluate(
            """element => element.childNodes[0].textContent"""
        ) as String?

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
                "Unable to parse Viranomainen product price: " +
                        "'$value' (normalized: '$normalized')",
                e
            )
        }
    }
}