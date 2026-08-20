package io.github.stscoundrel.caravansary.report

import io.github.stscoundrel.caravansary.ProductSource
import io.github.stscoundrel.caravansary.ProductTrackingResult

data class ProductStoreReport(
    val source: ProductSource,
    val sourceId: String,
    val result: ProductTrackingResult
)