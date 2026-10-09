package io.github.stscoundrel.caravansary.scraper

import com.microsoft.playwright.Browser

object ScraperConfig {
    val userAgent: String = System.getenv("CARAVANSARY_USER_AGENT")
        ?.takeIf { it.isNotBlank() }
        ?: "Caravansary/1.0"

    fun pageOptions(): Browser.NewPageOptions = Browser.NewPageOptions()
        .setUserAgent(userAgent)
}
