package io.github.stscoundrel.caravansary.application

import io.github.stscoundrel.caravansary.domain.ProductFetcher
import io.github.stscoundrel.caravansary.domain.ProductReportRepository
import io.github.stscoundrel.caravansary.report.ProductReport
import io.github.stscoundrel.caravansary.report.ProductReportService

class CaravansaryService(
    private val fetchers: List<ProductFetcher>,
    private val tracker: ProductTracker,
    private val reportService: ProductReportService,
    private val reportRepository: ProductReportRepository
) {

    fun run(): ProductReport {
        val results = fetchers.map { fetcher ->
            fetcher to tracker.run(fetcher)
        }

        val report = reportService.create(results)

        reportRepository.save(report)

        return report
    }
}