/**
 * Copyright 2023 Bram Wijnands
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
import kotlin.system.exitProcess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.audux.bgg.common.ThingType
import org.audux.bgg.module.BggKtorClient
import org.audux.bgg.module.BggXmlObjectMapper
import org.audux.bgg.module.appModule
import org.audux.bgg.request.Request
import org.audux.bgg.request.collection
import org.koin.core.Koin
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication

class BggClient : KoinComponent, AutoCloseable {
    internal val client: HttpClient by inject(named<BggKtorClient>())
    internal val mapper: ObjectMapper by inject(named<BggXmlObjectMapper>())
    private val clientScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        startKoin {
            logger(KermitKoinLogger(Logger.withTag("koin")))

            modules(appModule)
        }
    }

    /** Override Koin to get an isolated Koin context, see: [BggClientKoinContext]. */
    override fun getKoin(): Koin = BggClientKoinContext.koin

    /** Closes the [HttpClient] client after use. */
    override fun close() {
        client.close()
    }

    /** Calls/Launches a request async, once a response is available it will call [response]. */
    internal fun <R> call(request: suspend () -> R, response: (R) -> Unit) {
        clientScope.launch { response(request()) }
    }

    /** Returns a wrapped request for later execution. */
    internal fun <R> request(request: suspend () -> R) = Request(this, request)

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            BggClient().use { client ->
                repeat(10) {
                    client
                        .collection(
                            "Novaeux",
                            ThingType.BOARD_GAME,
                            excludeSubType = ThingType.BOARD_GAME_EXPANSION
                        )
                        .call {}
                }

                runBlocking {
                    delay(20_000)
                    exitProcess(0)
                }
            }
        }
    }
}

/** Isolated Koin Context for BGG Client. */
object BggClientKoinContext {
    private val koinApp = koinApplication { modules(appModule) }

    val koin = koinApp.koin
}

/** Thrown whenever any exception is thrown during a request to BGG. */
class BggRequestException(message: String) : Exception(message)
