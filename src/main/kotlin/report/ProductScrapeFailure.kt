package io.github.stscoundrel.caravansary.report

import io.github.stscoundrel.caravansary.domain.ProductSource

data class ProductScrapeFailure(
    val source: ProductSource,
    val sourceId: String,
    val errorMessage: String
)
