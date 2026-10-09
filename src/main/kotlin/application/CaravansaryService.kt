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
        val results = fetchers.mapNotNull { fetcher ->
            try {
                fetcher to tracker.run(fetcher)
            } catch (exception: InterruptedException) {
                Thread.currentThread().interrupt()
                throw exception
            } catch (exception: Exception) {
                System.err.println(
                    "Failed to track ${fetcher.source.displayName} " +
                            "(${fetcher.sourceId}): ${exception.message}"
                )
                null
            }
        }

        val report = reportService.create(results)

        reportRepository.save(report)

        return report
    }
}
