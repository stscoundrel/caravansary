package io.github.stscoundrel.caravansary

import io.github.stscoundrel.caravansary.application.CaravansaryService
import io.github.stscoundrel.caravansary.application.ProductTracker
import io.github.stscoundrel.caravansary.database.Database
import io.github.stscoundrel.caravansary.database.SqliteProductReportRepository
import io.github.stscoundrel.caravansary.database.SqliteProductRepository
import io.github.stscoundrel.caravansary.report.ConsoleReportRenderer
import io.github.stscoundrel.caravansary.report.ProductReportService
import io.github.stscoundrel.caravansary.scraper.aawee.AaweeScraper
import io.github.stscoundrel.caravansary.scraper.asenurkka.AsenurkkaScraper
import io.github.stscoundrel.caravansary.scraper.asetalo.AsetaloScraper
import io.github.stscoundrel.caravansary.scraper.bestcoast.BestCoastScraper
import io.github.stscoundrel.caravansary.scraper.erakala.ErakalaScraper
import io.github.stscoundrel.caravansary.scraper.eratarvike.EratarvikeScraper
import io.github.stscoundrel.caravansary.scraper.fusil.FusilScraper
import io.github.stscoundrel.caravansary.scraper.ironpoint.IronPointScraper
import io.github.stscoundrel.caravansary.scraper.jennynase.JennynAseScraper
import io.github.stscoundrel.caravansary.scraper.laatuase.LaatuaseScraper
import io.github.stscoundrel.caravansary.scraper.oulunase.OulunAseScraper
import io.github.stscoundrel.caravansary.scraper.pphunt.PpHuntScraper
import io.github.stscoundrel.caravansary.scraper.viranomainen.ViranomainenScraper

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
        EratarvikeScraper("/46-aseet-kaytetyt"),
        IronPointScraper("/kytetyt-aseet-c-53.html"),
        ErakalaScraper(
            "/asekauppa-ja-ampumaurheilu/kaytetyt-aseet/kaytetyt-kivaarit/c/201050/"
        ),
        ErakalaScraper(
            "/asekauppa-ja-ampumaurheilu/kaytetyt-aseet/kaytetyt-pistoolit/c/201060/"
        ),
        OulunAseScraper(
            "/category/30/haulikot-puoliautomaatti?sort=search&per_page=100"
        ),
        AsenurkkaScraper(
            "/tuote-osasto/kaikkituotteet/aseet/kaytetyt-aseet/"
        ),
    )


    Database("data/caravansary.db").use { database ->
        val productRepository =
            SqliteProductRepository(database.connection)

        val reportRepository =
            SqliteProductReportRepository(database.connection)

        val service = CaravansaryService(
            fetchers = fetchers,
            tracker = ProductTracker(productRepository),
            reportService = ProductReportService(),
            reportRepository = reportRepository
        )

        val report = service.run()

        ConsoleReportRenderer().render(report)
    }
}