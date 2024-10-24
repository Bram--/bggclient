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

/**
 * Ktor plugin to configure the client to limit the number of concurrent requests it can make.
 * Additional requests will be canceled and re-queued to be send whenever another request has been
 * completed.
 */
internal val ClientConcurrentRateLimitPlugin =
    createClientPlugin(
        "ClientConcurrentRateLimitPlugin",
        createConfiguration = ::ConcurrentRequestLimiterConfiguration,
    ) {
        val requestLimiter = ConcurrentRequestLimiter(pluginConfig.requestLimit)
        onRequest { request, _ -> requestLimiter.onNewRequest(request) }
    }

/**
 * Implementation of [ClientConcurrentRateLimitPlugin] ensuring not more than
 * [ConcurrentRequestLimiterConfiguration.requestLimit] are being made concurrently.
 */
internal class ConcurrentRequestLimiter(private val requestLimit: Int) {
    internal val inFlightRequests = AtomicInteger()

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
internal data class ConcurrentRequestLimiterConfiguration(var requestLimit: Int = 10)
