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
package org.audux.bgg.plugin

import co.touchlab.kermit.Logger
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import java.util.concurrent.atomic.AtomicInteger
import kotlinx.coroutines.delay
import org.jetbrains.annotations.Contract
import org.jetbrains.annotations.VisibleForTesting

/**
 * Ktor plugin to configure the client to limit the number of concurrent requests it can make.
 * Additional requests will be canceled and re-queued to be send whenever another request has been
 * completed.
 */
internal val ClientRateLimitPlugin =
    createClientPlugin(
        "ClientRateLimitPlugin",
        createConfiguration = ::ConcurrentRequestLimiterConfiguration
    ) {
        val requestLimiter = ConcurrentRequestLimiter(pluginConfig.requestLimit)
        onRequest { request, _ -> requestLimiter.onNewRequest(request) }
    }

/**
 * Implementation of [ClientRateLimitPlugin] ensuring not more than
 * [ConcurrentRequestLimiterConfiguration.requestLimit] are being made concurrently.
 */
internal class ConcurrentRequestLimiter(private val requestLimit: Int) {
    internal val inFlightRequests = AtomicSingletonInteger.instance

    /**
     * Keeps an counter for the number of requests that are active/in-flight. If ever the limit is
     * reached the request is held indefinitely until this request or other requests are
     * completed/cancelled.
     *
     * Called on [io.ktor.client.plugins.api.ClientPluginBuilder.onRequest].
     */
    suspend fun onNewRequest(request: HttpRequestBuilder) {
        logger.v(tag = "ConcurrentRequestLimiter") { "#OnNewRequest()" }
        // Ensure inFlight requests count is decremented whenever a request [Job] completes.
        request.executionContext.invokeOnCompletion {
            logger.v(tag = "ConcurrentRequestLimiter") { "Request completed" }
            inFlightRequests.decrementAndGet()
        }

        // Check whether the `inFlightRequests` count has been reached, if so delay and check again.
        do {
            val currentInFlightRequests = inFlightRequests.get()
            if (currentInFlightRequests < requestLimit) {
                inFlightRequests.incrementAndGet()
                break
            }

            logger.v(tag = "ConcurrentRequestLimiter") {
                "Concurrent Requests limit reached[$currentInFlightRequests/$requestLimit]"
            }

            // Delay and yield as we've reached the concurrent request limit.
            delay(IDLING_DELAY)
        } while (true)
    }

    companion object {
        private val logger = Logger.withTag("ClientRateLimitPlugin")
        private const val IDLING_DELAY = 50L
    }
}

/**
 * Configuration for the concurrent request limiter.
 *
 * @property requestLimit The maximum number of concurrent requests that can be made.
 */
internal class ConcurrentRequestLimiterConfiguration {
    var requestLimit: Int = 10
}

/**
 * Singleton [AtomicInteger] to be shared between [org.audux.bgg.BggClient] instances as a new
 * instance of the plugin is created for each client.
 */
internal class AtomicSingletonInteger
@Contract(pure = true)
private constructor(initialValue: Int) : AtomicInteger(initialValue) {
    companion object {
        val instance: AtomicSingletonInteger by lazy { AtomicSingletonInteger(0) }
    }

    /** Clears the in-flight requests counter. */
    @VisibleForTesting fun reset() = this.set(0)

    override fun toByte() = get().toByte()

    override fun toShort() = get().toShort()
}
