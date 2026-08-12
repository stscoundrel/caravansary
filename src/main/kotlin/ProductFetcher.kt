package io.github.stscoundrel.caravansary

interface ProductFetcher {
    val source: ProductSource
    val sourceId: String

    fun fetchProducts(): List<Product>
}