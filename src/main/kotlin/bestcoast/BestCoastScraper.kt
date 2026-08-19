package io.github.stscoundrel.caravansary.bestcoast

import com.microsoft.playwright.Locator
import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.github.stscoundrel.caravansary.Product
import io.github.stscoundrel.caravansary.ProductFetcher
import io.github.stscoundrel.caravansary.ProductSource
import java.math.BigDecimal

class BestCoastScraper(
    override val sourceId: String
) : ProductFetcher {

    companion object {
        private const val BASE_URL = "https://bestcoast.fi"
    }

    override val source = ProductSource.BEST_COAST

    override fun fetchProducts(): List<Product> {
        val url = "$BASE_URL$sourceId"

        Playwright.create().use { playwright ->
            playwright.chromium().launch().use { browser ->
                val page = browser.newPage()

                page.navigate(url)

                val pageUrls = getPageUrls(page)

                return pageUrls.flatMapIndexed { index, pageUrl ->
                    fetchPage(
                        page = page,
                        pageUrl = pageUrl,
                        pageNumber = index + 1
                    )
                }
            }
        }
    }

    private fun getPageUrls(page: Page): List<String> {
        val pagination = page.locator(".elementor-pagination")
        val links = pagination.locator("a.page-numbers")

        return (0 until links.count())
            .mapNotNull { i ->
                links.nth(i).getAttribute("href")
            }
            .filter { it.isNotBlank() }
            .distinct()
            .toMutableList()
            .apply {
                add(0, page.url())
            }
            .distinct()
    }

    private fun fetchPage(
        page: Page,
        pageUrl: String,
        pageNumber: Int
    ): List<Product> {
        page.navigate(pageUrl)

        val products = page.locator(".e-loop-item.product")
        val productCount = products.count()

        return (0 until productCount).map { i ->
            val product = products.nth(i)

            Product(
                source = ProductSource.BEST_COAST,
                sourceId = sourceId,
                id = product
                    .locator("a")
                    .first()
                    .getAttribute("href")
                    ?: "",
                name = product
                    .locator(".product_title")
                    .first()
                    .textContent()
                    ?.trim() ?: "",
                price = parsePrice(product),
                date = null
            )
        }
    }

    private fun parsePrice(product: Locator): BigDecimal {
        val price = product.locator(".woocommerce-Price-amount")

        if (price.count() == 0) {
            return BigDecimal.ZERO
        }

        val value = price
            .first()
            .textContent()

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
                "Unable to parse Best Coast product price: " +
                        "'$value' (normalized: '$normalized')",
                e
            )
        }
    }
}