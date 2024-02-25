plugins {
    kotlin("jvm") version "1.9.22"
    id("application")
}

group = "org.audux.bgg.examples"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

application {
    mainClass = "org.audux.bgg.examples.paginate.Main"
}

dependencies {
    implementation("org.audux.bgg:bggclient:0.5.0")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
