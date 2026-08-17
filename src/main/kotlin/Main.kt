package io.github.stscoundrel.caravansary

import io.github.stscoundrel.caravansary.aawee.AaweeScraper
import io.github.stscoundrel.caravansary.asetalo.AsetaloScraper
import io.github.stscoundrel.caravansary.database.Database
import io.github.stscoundrel.caravansary.database.SqliteProductRepository

fun main() {
    val fetchers = listOf(
        AsetaloScraper("/aseet/kaytetyt-aseet/sotilaskivaarit-tt2/7852/"),
        AsetaloScraper("/aseet/kaytetyt-aseet/kivaarit/49/"),
        AsetaloScraper("/aseet/kaytetyt-aseet/pistoolit/7850/"),
        AaweeScraper("/kaytetyt-tuotteet/kaytetyt-tuotteet/kaytetyt-aseet/kaytetyt-kivaarit/c/1100101/"),
        AaweeScraper("/ammunta-ja-aseet/aseet/kivaarit/itselataavat-kivaarit/c/100104/"),
        AaweeScraper("/ammunta-ja-aseet/aseet/pistoolit-ja-revolverit/itselataavat-pistoolit/c/100401/")
    )


    Database("data/caravansary.db").use { database ->
        val repository = SqliteProductRepository(database.connection)
        val tracker = ProductTracker(repository)

        fetchers.forEach { fetcher ->
            val result = tracker.run(fetcher)

            println("Current: ${result.currentProducts.size}")
            println("New: ${result.newProducts.size}")
            println("Sold out: ${result.soldOutProducts.size}")

            result.newProducts.forEach {
                println("NEW: ${it.name} - ${it.price}")
            }

            result.soldOutProducts.forEach {
                println("SOLD OUT: ${it.name} - ${it.price}")
            }
        }
    }
}