# Caravansary

Track new & sold-out firearms across a dozen different online stores. Mostly tracks used weapons that wont generally be "in stock" apart from the one unit. Product data is scraped, parsed into common format and stored for reporting.

## Usage

Kotlin project build using Gradle.

 `./gradlew run`

All scrapers use the shared user agent configured in
`src/main/kotlin/scraper/ScraperConfig.kt`, defaulting to `Caravansary/1.0`.
Override it with the `CARAVANSARY_USER_AGENT` environment variable:

```sh
CARAVANSARY_USER_AGENT='Caravansary/1.0 (+https://your-project.example)' ./gradlew run
```

## Whats in the name?

A caravansary is historically a resting place where travelers, merchants, and their goods converge. Inspired by a Kitaro track of the same name.
