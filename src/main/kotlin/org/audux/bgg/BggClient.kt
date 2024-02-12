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
package org.audux.bgg

import co.touchlab.kermit.Logger
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.audux.bgg.plugin.ClientRateLimitPlugin
import org.audux.bgg.request.Request
import org.audux.bgg.request.user
import org.audux.bgg.response.Response
import org.jetbrains.annotations.VisibleForTesting

/**
 * Unofficial Board Game Geek API Client for the
 * [BGG XML2 API2](https://boardgamegeek.com/wiki/page/BGG_XML_API2).
 *
 * <p>The actual BGG API can be interacted with, with the use of the extension functions in
 * [org.audux.bgg.request]. For example to do a search import [org.audux.bgg.request.search].
 *
 * <p>Search example usage:
 * ```
 * BggClient().use { client ->
 *          client
 *              .search("Scythe", arrayOf(ThingType.BOARD_GAME, ThingType.BOARD_GAME_EXPANSION))
 *              .call { response -> println(response) }
 * ```
 *
 * <p>Available API Calls:
 * <ul>
 * </ul>
 */
class BggClient {
    internal val mapper: ObjectMapper = newMapper()
    private val clientScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    /**
     * Calls/Launches a request async, once a response is available it will call [responseCallback].
     */
    internal fun <T> callAsync(request: suspend BggClient.() -> T, responseCallback: (T) -> Unit) =
        clientScope.launch {
            println("Launched?")

            try {
                val response = request.invoke(this@BggClient)
                println(response)
                withContext(Dispatchers.Default) {
                    println("DEFAULT")

                    try {
                        responseCallback(response)
                    } catch (e: Exception) {
                        println(e)
                        throw e
                    }
                }
            } catch (e: Exception) {
                print(e)
                throw e
            }
        }

    /** Calls/Launches a request and returns it's response. */
    internal suspend fun <T> call(request: suspend BggClient.() -> Response<T>) = request.invoke(this)

    /** Returns a wrapped request for later execution. */
    internal fun <T> request(request: suspend BggClient.() -> Response<T>) = Request(this, request)

    /**
     * Returns the current [io.ktor.client.engine.HttpClientEngine] used by this client. Used for
     * testing only.
     */
    //    @VisibleForTesting internal fun engine() = client.engine

    internal fun client() = buildClient()

    @VisibleForTesting
    internal fun buildClient() =
        HttpClient(CIO.create()) {
            install(ClientRateLimitPlugin) { requestLimit = 10 }
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

            expectSuccess = true
        }

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            BggClient().also { client ->
                try {
                    client.user("test").callAsync { println(it) }
                } catch (e: Exception) {
                    println(e)
                    e.printStackTrace()
                }
            }

            Thread.sleep(10_000)
        }

        /** Logging level Severity for the BGGClient logging. */
        enum class Severity {
            Verbose,
            Debug,
            Info,
            Warn,
            Error,
            Assert
        }

        /** Sets the Logger severity defaults to [Severity.Error] */
        @JvmStatic
        fun setLoggerSeverity(severity: Severity) {
            Logger.setMinSeverity(
                when (severity) {
                    Severity.Assert -> co.touchlab.kermit.Severity.Assert
                    Severity.Debug -> co.touchlab.kermit.Severity.Debug
                    Severity.Error -> co.touchlab.kermit.Severity.Error
                    Severity.Info -> co.touchlab.kermit.Severity.Info
                    Severity.Verbose -> co.touchlab.kermit.Severity.Verbose
                    Severity.Warn -> co.touchlab.kermit.Severity.Warn
                }
            )
        }
    }
}

object BggClient2 {
    /** Returns a wrapped request for later execution. */
    internal fun <T> request(request: suspend BggClient.() -> Response<T>) = Request(
        BggClient(), request)
}

internal fun newMapper() =
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
        .build()

/** Thrown whenever any exception is thrown during a request to BGG. */
class BggRequestException(message: String) : Exception(message)
