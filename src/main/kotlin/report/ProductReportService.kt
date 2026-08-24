package io.github.stscoundrel.caravansary.report

import io.github.stscoundrel.caravansary.ProductFetcher
import io.github.stscoundrel.caravansary.ProductTrackingResult
import java.time.LocalDateTime

class ProductReportService {

    fun create(
        results: List<Pair<ProductFetcher, ProductTrackingResult>>
    ): ProductReport {
        return ProductReport(
            generatedAt = LocalDateTime.now(),
            stores = results.map { (fetcher, result) ->
                ProductStoreReport(
                    source = fetcher.source,
                    sourceId = fetcher.sourceId,
                    currentCount = result.currentProducts.size,
                    newProducts = result.newProducts,
                    soldOutProducts = result.soldOutProducts
                )
            }
        )
    }
}