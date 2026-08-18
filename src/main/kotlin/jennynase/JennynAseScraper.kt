package io.github.stscoundrel.caravansary.jennynase

import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.github.stscoundrel.caravansary.Product
import io.github.stscoundrel.caravansary.ProductFetcher
import io.github.stscoundrel.caravansary.ProductSource
import java.math.BigDecimal

class JennynAseScraper(
    override val sourceId: String
) : ProductFetcher {

    companion object {
        private const val BASE_URL = "https://www.jennynase.fi"
    }

    override val source = ProductSource.JENNYN_ASE

    override fun fetchProducts(): List<Product> {
        val url = "$BASE_URL$sourceId"

        Playwright.create().use { playwright ->
            playwright.chromium().launch().use { browser ->
                val page = browser.newPage()

                page.navigate(url)

                val pageUrls = getPageUrls(page)

                println("Found ${pageUrls.size} pages")

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
        val pagination = page.locator(".pagination")
        val links = pagination.locator("a")

        return (0 until links.count())
            .mapNotNull { i ->
                links.nth(i).getAttribute("href")
            }
            .filter { href ->
                href.isNotBlank() &&
                        !href.startsWith("javascript:")
            }
            .map { href ->
                if (href.startsWith("http")) {
                    href
                } else {
                    "$BASE_URL$href"
                }
            }
            .distinct()
    }

    private fun fetchPage(
        page: Page,
        pageUrl: String,
        pageNumber: Int
    ): List<Product> {
        page.navigate(pageUrl)

        val products = page.locator(".wb-store-item")
        val productCount = products.count()

        println("Page $pageNumber: found $productCount products")

        return (0 until productCount).map { i ->
            val product = products.nth(i)

            Product(
                source = ProductSource.JENNYN_ASE,
                sourceId = sourceId,
                id = product.getAttribute("data-item-id") ?: "",
                name = product
                    .locator(".wb-store-name")
                    .textContent()
                    ?.trim() ?: "",
                price = parsePrice(
                    product
                        .locator(".wb-store-price")
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
                "Unable to parse Jennyn Ase product price: " +
                        "'$value' (normalized: '$normalized')",
                e
            )
        }
    }
}