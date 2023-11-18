package org.audux.bgg.module

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import org.koin.core.module.dsl.named
import org.koin.core.module.dsl.withOptions
import org.koin.dsl.module
import java.util.Locale

/** Used to ensure usage of correct Jackson [com.fasterxml.jackson.databind.ObjectMapper]. */
annotation class BggXmlObjectMapper()

/** Used to ensure usage of correct Ktor [com.fasterxml.jackson.databind.ObjectMapper]. */
annotation class BggKtorClient()

/**
 * Main Koin module for BggClient.
 */
val appModule = module {
    single {
        XmlMapper.builder().apply {
            configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)

            addModule(JacksonXmlModule())
            addModule(JavaTimeModule())

            // Keep hardcoded to US: https://bugs.openjdk.org/browse/JDK-8251317
            // en_GB Locale uses 'Sept' as a shortname when formatting dates (e.g. 'MMM'). The
            // locale en_US remains 'Sep'.
            defaultLocale(Locale.US)
            defaultUseWrapper(false)
        }.build().registerKotlinModule()
    } withOptions {
        named<BggXmlObjectMapper>()
    }

    single {
        HttpClient(OkHttp) {
            install(HttpRequestRetry) {
                retryOnServerErrors(maxRetries = 5)
                exponentialDelay()
            }
            expectSuccess = true
        }
    } withOptions {
        named<BggKtorClient>()
    }
}
