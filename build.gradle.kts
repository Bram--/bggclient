plugins {
  id("java-library")
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
}


repositories {
  mavenCentral()
}

dependencies {
  implementation(libs.jackson.kotlin)
  implementation(libs.jackson.jsr310)
  implementation(libs.jackson.xml)
  implementation(libs.koin.core)
  implementation(libs.kotlin.datetime)
  implementation(libs.kotlin.serialization)
  implementation(libs.ktor.core)
  implementation(libs.ktor.client)
  implementation(libs.napier)
  implementation(libs.slf4j)
}
