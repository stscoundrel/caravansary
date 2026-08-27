package io.github.stscoundrel.caravansary.scraper.ironpoint

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.github.stscoundrel.caravansary.domain.Product
import io.github.stscoundrel.caravansary.domain.ProductFetcher
import io.github.stscoundrel.caravansary.domain.ProductSource
import java.math.BigDecimal

class IronPointScraper(
    override val sourceId: String
) : ProductFetcher {

    companion object {
        private const val BASE_URL = "https://www.ironpoint.fi/fi"
    }

    override val source = ProductSource.IRON_POINT

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
        val links = page.locator(".pagination a")
        val linkCount = links.count()

        if (linkCount == 0) {
            return listOf(page.url())
        }

        return (0 until linkCount)
            .mapNotNull { i ->
                links.nth(i).getAttribute("href")
            }
            .filter { it.startsWith("http") }
            .distinct()
    }

    private fun scrapePage(
        page: Page,
        url: String
    ): List<Product> {
        page.navigate(url)

        val products = page.locator(".product-small")
        val productCount = products.count()

        return (0 until productCount).map { i ->
            val product = products.nth(i)

            val productLink = product
                .locator(".caption a")
                .first()

            Product(
                source = ProductSource.IRON_POINT,
                sourceId = sourceId,
                id = productLink.getAttribute("href") ?: "",
                name = productLink
                    .textContent()
                    ?.trim() ?: "",
                price = parsePrice(product),
                date = null
            )
        }
    }

    private fun parsePrice(product: Locator): BigDecimal {
        val price = product.locator(".price")

        if (price.count() == 0) {
            return BigDecimal.ZERO
        }

        val value = price.textContent()

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
                "Unable to parse Iron Point product price: '$value' " +
                        "(normalized: '$normalized')",
                e
            )
        }
    }
}