package io.github.stscoundrel.caravansary

import io.github.stscoundrel.caravansary.domain.ProductSource
import io.github.stscoundrel.caravansary.report.ConsoleReportRenderer
import io.github.stscoundrel.caravansary.report.ProductReport
import io.github.stscoundrel.caravansary.report.ProductScrapeFailure
import java.io.ByteArrayOutputStream
import java.io.PrintStream
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertFalse

class ConsoleReportRendererTest {
    @Test
    fun `an entirely failed run shows errors rather than zero inventory`() {
        val report = ProductReport(
            generatedAt = LocalDateTime.of(2026, 10, 9, 12, 0),
            stores = emptyList(),
            failures = listOf(
                ProductScrapeFailure(ProductSource.OULUN_ASE, "/category/30/", "Timeout")
            )
        )
        val output = ByteArrayOutputStream()
        val originalOut = System.out
        try {
            PrintStream(output).use { stream ->
                System.setOut(stream)
                ConsoleReportRenderer().render(report)
            }
        } finally {
            System.setOut(originalOut)
        }

        val rendered = output.toString()
        assertContains(rendered, "Oulun ase")
        assertContains(rendered, "/category/30/")
        assertContains(rendered, "FAILED: Timeout")
        assertContains(rendered, "Completed: 0 categories succeeded, 1 failed")
        assertFalse(rendered.contains("Current:"))
    }
}
