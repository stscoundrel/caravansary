package io.github.stscoundrel.caravansary.scraper.eratarvike

import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.github.stscoundrel.caravansary.domain.Product
import io.github.stscoundrel.caravansary.domain.ProductFetcher
import io.github.stscoundrel.caravansary.domain.ProductSource
import java.math.BigDecimal

class EratarvikeScraper(
    override val sourceId: String
) : ProductFetcher {

    companion object {
        private const val BASE_URL = "https://www.eratarvike.fi/fi"
    }

    override val source = ProductSource.ERATARVIKE

    override fun fetchProducts(): List<Product> {
        val url = "$BASE_URL$sourceId"

        Playwright.create().use { playwright ->
            playwright.chromium().launch().use { browser ->
                val page = browser.newPage()

                page.navigate(url)

                val pageUrls = getPageUrls(page)

                return pageUrls.flatMap { pageUrl ->
                    scrapePage(page, pageUrl)
                }
            }
        }
    }

    private fun getPageUrls(page: Page): List<String> {
        val links = page.locator(
            ".pagination-list-container a.page-link"
        )

        val linkCount = links.count()

        if (linkCount == 0) {
            return listOf(page.url())
        }

        return (0 until linkCount)
            .mapNotNull { i ->
                links.nth(i).getAttribute("href")
            }
            .distinct()
    }

    private fun scrapePage(
        page: Page,
        url: String
    ): List<Product> {
        page.navigate(url)

        val products = page.locator("article.product-miniature")
        val productCount = products.count()

        return (0 until productCount).map { i ->
            val product = products.nth(i)

            Product(
                source = ProductSource.ERATARVIKE,
                sourceId = sourceId,
                id = product.getAttribute("data-id-product") ?: "",
                name = product
                    .locator(".product-miniature__title")
                    .textContent()
                    ?.trim() ?: "",
                price = parsePrice(
                    product
                        .locator(".product-miniature__price")
                        .textContent()
                ),
                date = null
            )
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
                "Unable to parse Erätarvike product price: '$value' " +
                        "(normalized: '$normalized')",
                e
            )
        }
    }
}