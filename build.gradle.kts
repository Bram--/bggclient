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
            version = "0.5.0"

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

    repositories {
        maven {
            name = "OSSRH"
            url = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
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
