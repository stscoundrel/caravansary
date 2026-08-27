package io.github.stscoundrel.caravansary.domain

import io.github.stscoundrel.caravansary.report.ProductReport

interface ProductReportRepository {

    fun save(report: ProductReport): Long

    fun findById(id: Long): ProductReport?

    fun findLatest(): ProductReport?

    fun findAll(): List<ProductReport>
}