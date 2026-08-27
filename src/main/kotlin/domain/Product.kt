package io.github.stscoundrel.caravansary.domain

import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate

data class Product(
    val source: ProductSource,
    val sourceId: String,
    val id: String,
    val name: String,
    val price: BigDecimal,
    val date: LocalDate?,
    val status: ProductStatus = ProductStatus.ACTIVE,
    val lastSeenAt: Instant? = null
)