package io.github.stscoundrel.caravansary

import io.github.stscoundrel.caravansary.domain.ProductFetcher
import io.github.stscoundrel.caravansary.domain.ProductRepository
import io.github.stscoundrel.caravansary.domain.ProductStatus
import io.github.stscoundrel.caravansary.domain.ProductTrackingResult

class ProductTracker(
    private val repository: ProductRepository
) {

    fun run(fetcher: ProductFetcher): ProductTrackingResult {
        val previousProducts = repository.findAll(
            source = fetcher.source,
            sourceId = fetcher.sourceId
        )

        val currentProducts = fetcher.fetchProducts()

        // Don't mark anything as sold out if the scrape returned nothing.
        if (currentProducts.isEmpty()) {
            return ProductTrackingResult(
                currentProducts = emptyList(),
                newProducts = emptyList(),
                soldOutProducts = emptyList()
            )
        }

        val previousIds = previousProducts
            .map { it.id }
            .toSet()

        val currentIds = currentProducts
            .map { it.id }
            .toSet()

        val newProducts = currentProducts.filter {
            it.id !in previousIds
        }

        val soldOutProducts = previousProducts.filter {
            it.id !in currentIds &&
                    it.status == ProductStatus.ACTIVE
        }

        repository.saveAll(currentProducts)
        repository.markSoldOut(soldOutProducts)

        return ProductTrackingResult(
            currentProducts = currentProducts,
            newProducts = newProducts,
            soldOutProducts = soldOutProducts
        )
    }
}