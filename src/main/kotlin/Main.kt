package io.github.stscoundrel.caravansary

import io.github.stscoundrel.caravansary.aawee.AaweeScraper
import io.github.stscoundrel.caravansary.asetalo.AsetaloScraper
import io.github.stscoundrel.caravansary.bestcoast.BestCoastScraper
import io.github.stscoundrel.caravansary.database.Database
import io.github.stscoundrel.caravansary.database.SqliteProductReportRepository
import io.github.stscoundrel.caravansary.database.SqliteProductRepository
import io.github.stscoundrel.caravansary.eratarvike.EratarvikeScraper
import io.github.stscoundrel.caravansary.fusil.FusilScraper
import io.github.stscoundrel.caravansary.jennynase.JennynAseScraper
import io.github.stscoundrel.caravansary.laatuase.LaatuaseScraper
import io.github.stscoundrel.caravansary.pphunt.PpHuntScraper
import io.github.stscoundrel.caravansary.report.ConsoleReportRenderer
import io.github.stscoundrel.caravansary.report.ProductReportService
import io.github.stscoundrel.caravansary.viranomainen.ViranomainenScraper

fun main() {
    val fetchers = listOf(
        AsetaloScraper("/aseet/kaytetyt-aseet/sotilaskivaarit-tt2/7852/"),
        AsetaloScraper("/aseet/kaytetyt-aseet/kivaarit/49/"),
        AsetaloScraper("/aseet/kaytetyt-aseet/pistoolit/7850/"),
        AaweeScraper("/kaytetyt-tuotteet/kaytetyt-tuotteet/kaytetyt-aseet/kaytetyt-kivaarit/c/1100101/"),
        AaweeScraper("/ammunta-ja-aseet/aseet/kivaarit/itselataavat-kivaarit/c/100104/"),
        AaweeScraper("/ammunta-ja-aseet/aseet/pistoolit-ja-revolverit/itselataavat-pistoolit/c/100401/"),
        JennynAseScraper("/Tuotteet/"),
        BestCoastScraper("/osasto/kivaarit/"),
        ViranomainenScraper(
            "/c9473/daniel-defense-kiväärit?_attribuutti%5Bvarastossa%5D=1"
        ),
        LaatuaseScraper("/osta/aseet/kivaarit/"),
        LaatuaseScraper("/osta/aseet/sotilaskivaarit/"),
        FusilScraper("/?category=2"),
        FusilScraper("/?category=5"),
        PpHuntScraper("/kaytetyt-kivaarit/"),
        EratarvikeScraper("/46-aseet-kaytetyt")
    )


    Database("data/caravansary.db").use { database ->
        val productRepository =
            SqliteProductRepository(database.connection)

        val reportRepository =
            SqliteProductReportRepository(database.connection)

        val tracker = ProductTracker(productRepository)
        val reportService = ProductReportService()

        val results = fetchers.map { fetcher ->
            fetcher to tracker.run(fetcher)
        }

        val report = reportService.create(results)

        reportRepository.save(report)

        ConsoleReportRenderer().render(report)
    }
}