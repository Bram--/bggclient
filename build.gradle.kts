plugins {
  id("java-library")
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
}


repositories {
  mavenCentral()
}

dependencies {
  implementation(libs.jackson.kotlin)
  implementation(libs.jackson.xml)
  implementation(libs.koin.core)
  implementation(libs.ktor.core)
  implementation(libs.ktor.client)
  implementation(libs.napier)

}
