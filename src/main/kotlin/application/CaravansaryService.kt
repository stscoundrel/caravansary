package io.github.stscoundrel.caravansary.application

import io.github.stscoundrel.caravansary.domain.ProductFetcher
import io.github.stscoundrel.caravansary.domain.ProductReportRepository
import io.github.stscoundrel.caravansary.report.ProductReport
import io.github.stscoundrel.caravansary.report.ProductReportService
import io.github.stscoundrel.caravansary.report.ProductScrapeFailure

class CaravansaryService(
    private val fetchers: List<ProductFetcher>,
    private val tracker: ProductTracker,
    private val reportService: ProductReportService,
    private val reportRepository: ProductReportRepository
) {

    fun run(): ProductReport {
        val failures = mutableListOf<ProductScrapeFailure>()
        val results = fetchers.mapNotNull { fetcher ->
            try {
                fetcher to tracker.run(fetcher)
            } catch (exception: InterruptedException) {
                Thread.currentThread().interrupt()
                throw exception
            } catch (exception: Exception) {
                val message = exception.message
                    ?.takeIf { it.isNotBlank() }
                    ?: exception.javaClass.simpleName
                failures += ProductScrapeFailure(
                    source = fetcher.source,
                    sourceId = fetcher.sourceId,
                    errorMessage = message
                )
                System.err.println(
                    "Failed to track ${fetcher.source.displayName} " +
                            "(${fetcher.sourceId}): $message"
                )
                null
            }
        }

        val report = reportService.create(results).copy(failures = failures.toList())

        // Failure details are available only for the current run, not historical reports.
        reportRepository.save(report.copy(failures = emptyList()))

        return report
    }
}
