plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation("org.audux.bgg:bggclient:0.2")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}
tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

application {
    mainClass = "org.audux.bgg.examples.java.Main"
}

tasks.test {
    useJUnitPlatform()
}