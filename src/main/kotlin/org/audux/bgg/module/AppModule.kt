/**
 * Copyright 2023-2024 Bram Wijnands
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package org.audux.bgg.module

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import java.util.Locale
import org.audux.bgg.plugin.ClientRateLimitPlugin
import org.koin.core.qualifier.named
import org.koin.dsl.module

/** Used to ensure usage of correct Jackson [ObjectMapper]. */
annotation class BggXmlObjectMapper()

/** Used to ensure usage of correct Ktor [HttpClient]. */
annotation class BggKtorClient()

/** Used to ensure usage of correct Ktor [HttpClient]. */
annotation class BggHttpEngine()

/** Main Koin module for BggClient. */
val appModule = module {
    single(named<BggXmlObjectMapper>()) {
        XmlMapper.builder()
            .apply {
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)

                addModule(JacksonXmlModule())
                addModule(JavaTimeModule())
                addModule(
                    KotlinModule.Builder()
                        .enable(KotlinFeature.NullToEmptyCollection)
                        .enable(KotlinFeature.StrictNullChecks)
                        .build()
                )

                // Keep hardcoded to US: https://bugs.openjdk.org/browse/JDK-8251317
                // en_GB Locale uses 'Sept' as a shortname when formatting dates (e.g. 'MMM'). The
                // locale en_US remains 'Sep'.
                defaultLocale(Locale.US)
                defaultMergeable(true)
                defaultUseWrapper(false)
            }
            .build() as ObjectMapper
    }

    factory(named<BggHttpEngine>()) { CIO.create() }

    factory(named<BggKtorClient>()) {
        HttpClient(get(HttpClientEngine::class, named<BggHttpEngine>())) {
            install(HttpRequestRetry) {
                exponentialDelay()
                retryIf(maxRetries = 5) { _, response ->
                    response.status.value.let {
                        // Add 202 (Accepted) for retries, see:
                        // https://boardgamegeek.com/thread/1188687/export-collections-has-been-updated-xmlapi-develop
                        it in (500..599) + 202
                    }
                }
            }
            install(ClientRateLimitPlugin) { requestLimit = 10 }

            expectSuccess = true
        }
    }
}
