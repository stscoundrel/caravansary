package io.github.stscoundrel.caravansary.domain

interface ProductFetcher {
    val source: ProductSource
    val sourceId: String

    fun fetchProducts(): List<Product>
}