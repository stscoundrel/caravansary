package io.github.stscoundrel.caravansary

import io.github.stscoundrel.caravansary.aawee.AaweeScraper
import io.github.stscoundrel.caravansary.asetalo.AsetaloScraper
import io.github.stscoundrel.caravansary.bestcoast.BestCoastScraper
import io.github.stscoundrel.caravansary.database.Database
import io.github.stscoundrel.caravansary.database.SqliteProductRepository
import io.github.stscoundrel.caravansary.jennynase.JennynAseScraper
import io.github.stscoundrel.caravansary.laatuase.LaatuaseScraper
import io.github.stscoundrel.caravansary.report.ConsoleReportRenderer
import io.github.stscoundrel.caravansary.report.ProductReport
import io.github.stscoundrel.caravansary.report.ProductStoreReport
import io.github.stscoundrel.caravansary.viranomainen.ViranomainenScraper
import java.time.LocalDateTime

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
    )


    Database("data/caravansary.db").use { database ->
        val repository = SqliteProductRepository(database.connection)
        val tracker = ProductTracker(repository)

        val storeReports = fetchers.map { fetcher ->
            ProductStoreReport(
                source = fetcher.source,
                sourceId = fetcher.sourceId,
                result = tracker.run(fetcher)
            )
        }

        val report = ProductReport(
            generatedAt = LocalDateTime.now(),
            stores = storeReports
        )

        ConsoleReportRenderer().render(report)
    }
}