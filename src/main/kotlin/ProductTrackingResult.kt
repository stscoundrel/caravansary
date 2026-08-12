package io.github.stscoundrel.caravansary

data class ProductTrackingResult(
    val currentProducts: List<Product>,
    val newProducts: List<Product>,
    val soldOutProducts: List<Product>
)