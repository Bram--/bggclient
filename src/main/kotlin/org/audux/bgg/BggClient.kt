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
import co.touchlab.kermit.koin.KermitKoinLogger
import com.fasterxml.jackson.databind.ObjectMapper
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.audux.bgg.module.BggKtorClient
import org.audux.bgg.module.BggXmlObjectMapper
import org.audux.bgg.module.appModule
import org.audux.bgg.request.Request
import org.audux.bgg.request.user
import org.audux.bgg.response.Response
import org.jetbrains.annotations.VisibleForTesting
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication

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
class BggClient : KoinComponent, AutoCloseable {
    internal val client: HttpClient by inject(named<BggKtorClient>())
    internal val mapper: ObjectMapper by inject(named<BggXmlObjectMapper>())
    private val clientScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val koinContext = BggClientKoinContext()

    /** Override Koin to get an isolated Koin context, see: [BggClientKoinContext]. */
    override fun getKoin() = koinContext.koin

    /** Closes the [HttpClient] client after use. */
    override fun close() {
        client.close()
    }

    /**
     * Calls/Launches a request async, once a response is available it will call [responseCallback].
     */
<<<<<<< Updated upstream
    internal fun <T> callAsync(request: suspend () -> T, responseCallback: (T) -> Unit) {
        clientScope.launch {
            val response = request()
            withContext(Dispatchers.Default) { responseCallback(response) }
=======
    internal fun <T> callAsync(request: suspend () -> T, responseCallback: (T) -> Unit) =
        clientScope.launch {
            println("Launched?")

            try {
                val response = request()
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
>>>>>>> Stashed changes
        }
    }

    /** Calls/Launches a request and returns it's response. */
    internal suspend fun <T> call(request: suspend () -> Response<T>) = request()

    /** Returns a wrapped request for later execution. */
    internal fun <T> request(request: suspend () -> Response<T>) = Request(this, request)

    /**
     * Returns the current [io.ktor.client.engine.HttpClientEngine] used by this client. Used for
     * testing only.
     */
    @VisibleForTesting internal fun engine() = client.engine

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {
            BggClient().use { client ->
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

/** Isolated Koin Context for BGG Client. */
class BggClientKoinContext {
    private val koinApp = koinApplication {
        logger(KermitKoinLogger(Logger.withTag("koin")))
        modules(appModule)
    }

    val koin = koinApp.koin
}

/** Thrown whenever any exception is thrown during a request to BGG. */
class BggRequestException(message: String) : Exception(message)
