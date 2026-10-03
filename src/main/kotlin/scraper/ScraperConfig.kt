package io.github.stscoundrel.caravansary.scraper

import com.microsoft.playwright.Browser
import com.microsoft.playwright.BrowserContext

object ScraperConfig {
    const val USER_AGENT = "Caravansary/1.0 (+https://github.com/stscoundrel/caravansary)"
}

fun Browser.newScraperContext(): BrowserContext =
    this.newContext(
        Browser.NewContextOptions()
            .setUserAgent(ScraperConfig.USER_AGENT)
    )
