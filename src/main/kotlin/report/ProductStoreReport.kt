package io.github.stscoundrel.caravansary.report

import io.github.stscoundrel.caravansary.Product
import io.github.stscoundrel.caravansary.ProductSource

data class ProductStoreReport(
    val source: ProductSource,
    val sourceId: String,
    val currentCount: Int,
    val newProducts: List<Product>,
    val soldOutProducts: List<Product>
)