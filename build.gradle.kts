plugins {
  `java-library`
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  alias(libs.plugins.ktfmt.gradle)
}

ktfmt {
  kotlinLangStyle()

  removeUnusedImports.set(true)
}

tasks.test {
  useJUnitPlatform()
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(libs.jackson.jsr310)
  implementation(libs.jackson.kotlin)
  implementation(libs.jackson.xml)
  implementation(libs.koin.core)
  implementation(libs.kotlin.datetime)
  implementation(libs.kotlin.serialization)
  implementation(libs.ktor.core)
  implementation(libs.ktor.client)
  implementation(libs.napier)
  implementation(libs.slf4j)

  // Testing dependencies.
  testImplementation(libs.junit5.api)
  testImplementation(libs.koin.test)
  testImplementation(libs.koin.test.junit)
  testImplementation(libs.truth)

  testRuntimeOnly(libs.junit5.engine)
}
