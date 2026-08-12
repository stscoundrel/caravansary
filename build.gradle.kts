plugins {
    kotlin("jvm") version "2.1.20"
    application
}

group = "io.github.stscoundrel"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.microsoft.playwright:playwright:1.61.0")
    implementation("org.xerial:sqlite-jdbc:3.50.3.0")
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(21)
}
application {
    mainClass.set("io.github.stscoundrel.caravansary.MainKt")
}