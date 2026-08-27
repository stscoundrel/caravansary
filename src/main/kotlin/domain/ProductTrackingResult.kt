package io.github.stscoundrel.caravansary.domain

data class ProductTrackingResult(
    val currentProducts: List<Product>,
    val newProducts: List<Product>,
    val soldOutProducts: List<Product>
)