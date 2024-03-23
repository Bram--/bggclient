import org.jetbrains.dokka.gradle.DokkaTask
import java.net.URL

plugins {
    jacoco
    `java-library`
    `maven-publish`
    signing
    alias(libs.plugins.com.gradleup.nmcp)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ktfmt.gradle)
    alias(libs.plugins.org.jetbrains.dokka)
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            groupId = "org.audux.bgg"
            artifactId = "bggclient"
            version = "0.7.1.6"

            pom {
                name = "Unofficial JVM BGG client"
                description =
                    "Library to fetch data from the Board game geek (XML) APIs. Usable in Java and Kotlin on the JVM or Android."
                url = "https://github.com/Bram--/bggclient"
                licenses {
                    license {
                        name = "The Apache License, Version 2.0"
                        url = "http://www.apache.org/licenses/LICENSE-2.0.txt"
                    }
                }
                developers {
                    developer {
                        id = "bram--"
                        name = "Bram Wijnands"
                        email = "brambomail@gmail.com"
                    }
                }
                scm {
                    connection = "scm:git:git://git@github.com:Bram--/bggclient.git"
                    developerConnection = "scm:git:ssh:git@github.com:Bram--/bggclient.git"
                    url = "https://github.com/Bram--/bggclient"
                }
            }

            from(components["java"])
        }
    }
}

nmcp {
    publish("mavenJava") {
        username = System.getenv("MAVEN_USERNAME")
        password = System.getenv("MAVEN_PASSWORD")
        publicationType = "AUTOMATIC"
    }
}

signing {
    if (project.gradle.startParameter.taskNames.contains("publishAllPublicationsToCentralPortal")) {
        afterEvaluate {
            useInMemoryPgpKeys(
                System.getenv("GPG_SIGNING_KEY"),
                System.getenv("GPG_SIGNING_PASSWORD")
            )
            sign(publishing.publications["mavenJava"])
        }
    }
}

ktfmt {
    kotlinLangStyle()

    removeUnusedImports.set(true)
}

sourceSets { main { kotlin { exclude("examples/") } } }

repositories { mavenCentral() }

dependencies {
    api(libs.kotlin.coroutines.jdk8)

    implementation(libs.jackson.jsr310)
    implementation(libs.jackson.kotlin)
    implementation(libs.jackson.xml)
    implementation(libs.javax.xml.stream)
    implementation(libs.kermit)
    implementation(libs.kotlin.serialization)
    implementation(libs.ktor.core)
    implementation(libs.ktor.client)
    implementation(libs.slf4j)

    // Testing dependencies.
    testApi(libs.ktor.client.mock)

    testImplementation(libs.kotlin.serialization.json)
    testImplementation(libs.junit5.jupiter)
    testImplementation(libs.junit5.params)
    testImplementation(libs.truth)

    testRuntimeOnly(libs.junit5.engine)
}

jacoco {
    toolVersion = "0.8.9"
    reportsDirectory = layout.buildDirectory.dir("jacoco")
}

tasks {
    test {
        useJUnitPlatform()
        finalizedBy(jacocoTestReport)
    }

    jacocoTestReport {
        dependsOn(test)

        reports {
            xml.required.set(true)
            html.required.set(true)
        }
    }
}

tasks.withType<DokkaTask>().configureEach {
    dokkaSourceSets {
        named("main") {
            moduleName.set("BggClient")
            includes.from(project.files(), "MODULE.md", "PACKAGES.md")
            noStdlibLink.set(true)
            noJdkLink.set(true)
            noAndroidSdkLink.set(true)

            sourceLink {
                localDirectory.set(file("src/main/kotlin"))
                remoteUrl.set(
                    URL("https://github.com/Bram--/bggclient/tree/main")
                )
            }
        }
    }
}
