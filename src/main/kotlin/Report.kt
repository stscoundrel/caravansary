package io.github.stscoundrel.caravansary

class Report {

    fun print(
        fetcher: ProductFetcher,
        result: ProductTrackingResult
    ) {
        println()
        println("=".repeat(60))
        println(fetcher.source.displayName)
        println("  ${fetcher.sourceId}")
        println()

        println("  Current:   ${result.currentProducts.size}")
        println("  New:       ${result.newProducts.size}")
        println("  Sold out:  ${result.soldOutProducts.size}")

        if (result.newProducts.isNotEmpty()) {
            println()
            println("  NEW")

            result.newProducts.forEach {
                println("    ${it.name} — ${it.price} €")
            }
        }

        if (result.soldOutProducts.isNotEmpty()) {
            println()
            println("  SOLD OUT")

            result.soldOutProducts.forEach {
                println("    ${it.name} — ${it.price} €")
            }
        }
    }
}