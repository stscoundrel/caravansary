package io.github.stscoundrel.caravansary.scraper.laatuase

import com.microsoft.playwright.Page
import com.microsoft.playwright.Playwright
import io.github.stscoundrel.caravansary.domain.Product
import io.github.stscoundrel.caravansary.domain.ProductFetcher
import io.github.stscoundrel.caravansary.domain.ProductSource
import java.math.BigDecimal

class LaatuaseScraper(
    override val sourceId: String
) : ProductFetcher {

    companion object {
        private const val BASE_URL = "https://laatuase.fi"
    }

    override val source = ProductSource.LAATUASE

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
        val pagination = page.locator(".woocommerce-pagination")

        if (pagination.count() == 0) {
            return listOf(page.url())
        }

        val links = pagination.locator("a.page-numbers")

        return buildList {
            add(page.url())

            for (i in 0 until links.count()) {
                val href = links.nth(i).getAttribute("href")

                if (!href.isNullOrBlank()) {
                    add(href)
                }
            }
        }.distinct()
    }

    private fun fetchPage(
        page: Page,
        pageUrl: String,
        pageNumber: Int
    ): List<Product> {
        page.navigate(pageUrl)

        val products = page.locator(".product")
        val productCount = products.count()

        return (0 until productCount).map { i ->
            val product = products.nth(i)

            Product(
                source = ProductSource.LAATUASE,
                sourceId = sourceId,
                id = product
                    .locator("a")
                    .first()
                    .getAttribute("href")
                    ?: "",
                name = product
                    .locator(".woocommerce-loop-product__title")
                    .first()
                    .textContent()
                    ?.trim() ?: "",
                price = parsePrice(product),
                date = null
            )
        }
    }

    private fun parsePrice(product: com.microsoft.playwright.Locator): BigDecimal {
        val value = product
            .locator(".woocommerce-Price-amount")
            .first()
            .textContent()

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
                "Unable to parse Laatuase product price: " +
                        "'$value' (normalized: '$normalized')",
                e
            )
        }
    }
}