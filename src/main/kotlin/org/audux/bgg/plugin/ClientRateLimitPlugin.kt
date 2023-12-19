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
package org.audux.bgg.plugin

import co.touchlab.kermit.Logger
import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import org.audux.bgg.BggRequestException

/**
 * Ktor plugin to configure the client to limit the number of concurrent requests it can make.
 * Additional requests will be canceled and re-queued to be send whenever another request has been
 * completed.
 */
val ClientRateLimitPlugin =
    createClientPlugin(
        "ClientRateLimitPlugin",
        createConfiguration = ::ConcurrentRequestLimiterConfiguration
    ) {
        val requestLimiter = ConcurrentRequestLimiter(client, pluginConfig.requestLimit)
        onRequest { request, _ -> requestLimiter.onNewRequest(request) }
        onResponse { requestLimiter.onNewResponse() }
    }

/**
 * Implementation of [ClientRateLimitPlugin] ensuring not more than
 * [ConcurrentRequestLimiterConfiguration.requestLimit] are being made concurrently.
 */
class ConcurrentRequestLimiter(private val client: HttpClient, private val requestLimit: Int) {
    internal val inFlightRequests = AtomicInteger(0)
    internal val requestQueue = ConcurrentLinkedQueue<HttpRequestBuilder>()

    /**
     * Cancels the request and adds it to the [requestQueue] to be re-requested when a response
     * comes back.
     *
     * <p>Called on [io.ktor.client.plugins.api.ClientPluginBuilder.onRequest].
     */
    fun onNewRequest(request: HttpRequestBuilder) {
        logger.v("ConcurrentRequestLimiter#OnNewRequest()")
        if (inFlightRequests.get() >= requestLimit) {
            logger.d("Request limit met[limit=$requestLimit]: queueing request")
            request.executionContext.cancel()

            if (!requestQueue.add(HttpRequestBuilder().takeFrom(request))) {
                throw BggRequestException(
                    "Failed to queue request in ${ConcurrentRequestLimiter::class.simpleName}"
                )
            }
        } else {
            inFlightRequests.incrementAndGet()
        }
    }

    /**
     * Whenever a response comes back and there a [HttpRequestBuilder] objects in the queue,
     * [HttpClient.request] will be called.
     *
     * <p>Called on [io.ktor.client.plugins.api.ClientPluginBuilder.onResponse].
     */
    suspend fun onNewResponse() {
        logger.v("ConcurrentRequestLimiter#OnNewResponse()")
        if (inFlightRequests.decrementAndGet() < requestLimit && requestQueue.isNotEmpty()) {
            client.request(requestQueue.remove())
            logger.d("Sent request from RequestQueue[empty=${requestQueue.isNotEmpty()}}]")
        }
    }

    companion object {
        private val logger = Logger.withTag("ClientRateLimitPlugin")
    }
}

/**
 * Configuration for the concurrent request limiter.
 *
 * @property requestLimit The maximum number of concurrent requests that can be made.
 */
class ConcurrentRequestLimiterConfiguration {
    var requestLimit: Int = 10
}
