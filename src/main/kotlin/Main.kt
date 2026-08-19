package io.github.stscoundrel.caravansary

import io.github.stscoundrel.caravansary.aawee.AaweeScraper
import io.github.stscoundrel.caravansary.asetalo.AsetaloScraper
import io.github.stscoundrel.caravansary.bestcoast.BestCoastScraper
import io.github.stscoundrel.caravansary.database.Database
import io.github.stscoundrel.caravansary.database.SqliteProductRepository
import io.github.stscoundrel.caravansary.jennynase.JennynAseScraper

fun main() {
    val fetchers = listOf(
        AsetaloScraper("/aseet/kaytetyt-aseet/sotilaskivaarit-tt2/7852/"),
        AsetaloScraper("/aseet/kaytetyt-aseet/kivaarit/49/"),
        AsetaloScraper("/aseet/kaytetyt-aseet/pistoolit/7850/"),
        AaweeScraper("/kaytetyt-tuotteet/kaytetyt-tuotteet/kaytetyt-aseet/kaytetyt-kivaarit/c/1100101/"),
        AaweeScraper("/ammunta-ja-aseet/aseet/kivaarit/itselataavat-kivaarit/c/100104/"),
        AaweeScraper("/ammunta-ja-aseet/aseet/pistoolit-ja-revolverit/itselataavat-pistoolit/c/100401/"),
        JennynAseScraper("/Tuotteet/"),
        BestCoastScraper("/osasto/kivaarit/")
    )


    Database("data/caravansary.db").use { database ->
        val repository = SqliteProductRepository(database.connection)
        val tracker = ProductTracker(repository)
        val report = Report()

        fetchers.forEach { fetcher ->
            val result = tracker.run(fetcher)
            report.print(fetcher, result)
        }
    }
}