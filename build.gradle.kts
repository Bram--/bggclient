plugins {
  jacoco
  `java-library`
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  alias(libs.plugins.ktfmt.gradle)
}

ktfmt {
  kotlinLangStyle()

  removeUnusedImports.set(true)
}

repositories {
  mavenCentral()
}

dependencies {
  implementation(libs.jackson.jsr310)
  implementation(libs.jackson.kotlin)
  implementation(libs.jackson.xml)
  implementation(libs.kermit)
  implementation(libs.kermit.koin)
  implementation(libs.koin.core)
  implementation(libs.kotlin.datetime)
  implementation(libs.kotlin.serialization)
  implementation(libs.ktor.core)
  implementation(libs.ktor.client)
  implementation(libs.slf4j)

  // Testing dependencies.
  testImplementation(libs.junit5.api)
  testImplementation(libs.koin.test)
  testImplementation(libs.koin.test.junit)
  testImplementation(libs.ktor.client.mock)
  testImplementation(libs.truth)

  testRuntimeOnly(libs.junit5.engine)
}

tasks.test {
  useJUnitPlatform()
  finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

jacoco {
  toolVersion = "0.8.9"
  reportsDirectory =  layout.buildDirectory.dir("jacoco")
}
