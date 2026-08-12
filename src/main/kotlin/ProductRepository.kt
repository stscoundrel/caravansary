package io.github.stscoundrel.caravansary

interface ProductRepository {

    fun findAll(
        source: ProductSource,
        sourceId: String
    ): List<Product>

    fun saveAll(products: List<Product>)

    fun markSoldOut(products: List<Product>)
}