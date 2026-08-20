package io.github.stscoundrel.caravansary.report

class ConsoleReportRenderer {

    fun render(report: ProductReport) {
        println()
        println("=".repeat(60))
        println("Caravansary Product Report")
        println("Generated: ${report.generatedAt}")
        println("=".repeat(60))

        report.stores.forEach { store ->
            renderStore(store)
        }
    }

    private fun renderStore(store: ProductStoreReport) {
        val result = store.result

        println()
        println("-".repeat(60))
        println(store.source.displayName)
        println("  ${store.sourceId}")
        println()

        println("  Current:   ${result.currentProducts.size}")
        println("  New:       ${result.newProducts.size}")
        println("  Sold out:  ${result.soldOutProducts.size}")

        if (result.newProducts.isNotEmpty()) {
            println()
            println("  NEW")

            result.newProducts.forEach { product ->
                println("    ${product.name} — ${product.price} €")
            }
        }

        if (result.soldOutProducts.isNotEmpty()) {
            println()
            println("  SOLD OUT")

            result.soldOutProducts.forEach { product ->
                println("    ${product.name} — ${product.price} €")
            }
        }
    }
}