package io.github.stscoundrel.caravansary.scraper.oulunase

import com.microsoft.playwright.Playwright
import io.github.stscoundrel.caravansary.domain.Product
import io.github.stscoundrel.caravansary.domain.ProductFetcher
import io.github.stscoundrel.caravansary.domain.ProductSource
import java.math.BigDecimal

class OulunAseScraper(
    override val sourceId: String
) : ProductFetcher {

    companion object {
        private const val BASE_URL = "https://www.oulunase.fi"
    }

    override val source = ProductSource.OULUN_ASE

    override fun fetchProducts(): List<Product> {
        val url = "$BASE_URL$sourceId"

        Playwright.create().use { playwright ->
            playwright.chromium().launch().use { browser ->
                val page = browser.newPage()

                page.navigate(url)

                page.locator(".ProductList").waitFor()

                val products = page.locator(".ProductList .ListItem")
                val productCount = products.count()

                return (0 until productCount).mapNotNull { i ->
                    val product = products.nth(i)

                    if (!product
                            .textContent()
                            .orEmpty()
                            .contains("Lisää ostoskoriin")
                    ) {
                        return@mapNotNull null
                    }

                    val productLink = product
                        .locator("a")
                        .first()

                    Product(
                        source = ProductSource.OULUN_ASE,
                        sourceId = sourceId,
                        id = productLink
                            .getAttribute("href")
                            ?: "",
                        name = product
                            .locator(".ProductName")
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
        val discountPrice = product.locator(".ProductDiscountPrice")

        val currentPrice = product.locator(".ProductCurrentPrice")

        val value = when {
            discountPrice.count() > 0 ->
                discountPrice.textContent()

            currentPrice.count() > 0 ->
                currentPrice.textContent()

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
            .replace("Alkaen", "")
            .trim()

        return try {
            BigDecimal(normalized)
        } catch (e: NumberFormatException) {
            throw IllegalArgumentException(
                "Unable to parse Oulun Ase product price: '$value' " +
                        "(normalized: '$normalized')",
                e
            )
        }
    }
}