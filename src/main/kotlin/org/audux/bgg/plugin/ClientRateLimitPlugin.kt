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
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.TimeMark
import kotlin.time.TimeSource
import kotlinx.coroutines.delay
import org.jetbrains.annotations.VisibleForTesting

/**
 * Ktor client rate limit plugin.
 *
 * <p>This configures the client to limit the number of requests it can make per [Duration]. For
 * example the plugin can be configured to do 60 requests per minute, or 1 request per second etc.
 */
internal val ClientRateLimitPlugin =
    createClientPlugin(
        "ClientRateLimitPlugin",
        createConfiguration = ::RequestLimiterConfiguration,
    ) {
        val requestLimiter = RequestLimiter(pluginConfig.requestLimit, pluginConfig.windowSize)
        onRequest { request, _ -> requestLimiter.onNewRequest(request) }
    }

/**
 * Implementation of [ClientRateLimitPlugin] ensuring not more than [requestLimit] are being within
 * the period of [windowLength].
 */
internal class RequestLimiter(private val requestLimit: Int, private val windowLength: Duration) {
    private val timeSource: TimeSource = TimeSource.Monotonic
    private var currentWindowStart: AtomicReference<TimeMark?> = AtomicReference()
    private var requestsInDelayed: AtomicInteger = AtomicInteger(0)

    @VisibleForTesting internal var requestsInCurrentWindow: AtomicInteger = AtomicInteger(0)

    suspend fun onNewRequest(request: HttpRequestBuilder) {
        do {
            var windowStart = currentWindowStart.get()
            if (windowStart == null || windowStart.plus(windowLength).hasPassedNow()) {
                windowStart = timeSource.markNow()
                currentWindowStart.set(windowStart)
                requestsInCurrentWindow.set(0)
            }

            val requestsMadeInWindow = requestsInCurrentWindow.get()
            if (requestsMadeInWindow < requestLimit) {
                requestsInCurrentWindow.incrementAndGet()
                break
            }

            val requestsBeingDelayed = requestsInDelayed.incrementAndGet()
            logger.i(tag = "RequestLimiter") {
                "Requests limit for window reached[$requestsMadeInWindow/$requestLimit]"
            }

            // Add `requestsBeingDelayed`ms to the delay to ensure requests retain order after
            // delay.
            delay(
                windowLength.minus(windowStart.elapsedNow()).plus(requestsBeingDelayed.milliseconds)
            )
            requestsBeingDelayed.dec()
        } while (true)
    }

    companion object {
        private val logger = Logger.withTag("ClientRateLimitPlugin")
    }
}

/**
 * Configuration for [RequestLimiter]
 *
 * @property requestLimit Throttles the client to have [requestLimit] request per [windowSize], e.g.
 *   "60 requests per 60.seconds".
 * @property windowSize Throttles the client to have [requestLimit] request per [windowSize], e.g.
 *   "60 requests per 60.seconds".
 */
internal data class RequestLimiterConfiguration(
    var requestLimit: Int = 60,
    var windowSize: Duration = 60.seconds,
)
