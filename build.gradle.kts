plugins {
    jacoco
    `java-library`
    `maven-publish`
    alias(libs.plugins.org.jetbrains.kotlin.jvm)
    alias(libs.plugins.ktfmt.gradle)
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            groupId = "org.audux.bgg"
            artifactId = "bggclient"
            version = "0.4.1"

            pom {
                name = "Unofficial JVM BGG client"
                description = "Wrapper around the Board Game Geek's XML1 and XML2 APIs"
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
    implementation(libs.kotlin.datetime)
    implementation(libs.kotlin.serialization)
    implementation(libs.ktor.core)
    implementation(libs.ktor.client)
    implementation(libs.slf4j)

    // Testing dependencies.
    testApi(libs.ktor.client.mock)

    testImplementation(libs.junit5.api)
    testImplementation(libs.truth)

    testRuntimeOnly(libs.junit5.engine)
}

tasks.test {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}

jacoco {
    toolVersion = "0.8.9"
    reportsDirectory = layout.buildDirectory.dir("jacoco")
}
