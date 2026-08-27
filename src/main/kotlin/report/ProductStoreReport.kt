package io.github.stscoundrel.caravansary.report

import io.github.stscoundrel.caravansary.domain.Product
import io.github.stscoundrel.caravansary.domain.ProductSource

data class ProductStoreReport(
    val source: ProductSource,
    val sourceId: String,
    val currentCount: Int,
    val newProducts: List<Product>,
    val soldOutProducts: List<Product>
)