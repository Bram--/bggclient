plugins {
  jacoco
  `java-library`
  `maven-publish`
  alias(libs.plugins.org.jetbrains.kotlin.jvm)
  alias(libs.plugins.ktfmt.gradle)
}

publishing {
  publications {
    create<MavenPublication>("maven") {
      groupId = "org.audux.bgg"
      artifactId = "bggclient"
      version = "0.2"

      from(components["kotlin"])
    }
  }
}

ktfmt {
  kotlinLangStyle()

  removeUnusedImports.set(true)
}

sourceSets {
  main {
    kotlin {
      exclude("examples/")
    }
  }
}

repositories {
  mavenCentral()
}

dependencies {
  api(libs.koin.core)

  implementation(libs.jackson.jsr310)
  implementation(libs.jackson.kotlin)
  implementation(libs.jackson.xml)
  implementation(libs.javax.xml.stream)
  implementation(libs.kermit)
  implementation(libs.kermit.koin)
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
