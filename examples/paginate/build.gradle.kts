plugins {
    id("java")
    id("application")

    id("org.jetbrains.kotlin.jvm") version "1.9.10"
}

group = "org.audux.bgg.examples.paginate"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.audux.bgg:bggclient:0.3.1")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

application {
    mainClass = "org.audux.bgg.examples.paginate.Main\$Companion"
}

tasks.test {
    useJUnitPlatform()
}
