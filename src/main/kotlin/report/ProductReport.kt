package io.github.stscoundrel.caravansary.report

import java.time.LocalDateTime

data class ProductReport(
    val generatedAt: LocalDateTime,
    val stores: List<ProductStoreReport>,
    val failures: List<ProductScrapeFailure> = emptyList()
)
