package org.audux.bgg.schedule

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger
import org.audux.bgg.BggRequestException

/**
 * Configure the client to limit the number of concurrent requests it can make. Additional requests
 * will be canceled and re-queued to be send whenever another request has been completed.
 */
val ClientRateLimitPlugin =
    createClientPlugin(
        "ClientRateLimitPlugin",
        createConfiguration = ::ConcurrentRequestLimiterConfiguration
    ) {
        val rateLimiter = ConcurrentRequestLimiter(client, pluginConfig.requestLimit)
        onRequest { request, _ -> rateLimiter.onNewRequest(request) }
        onResponse { rateLimiter.onNewResponse() }
    }

/**
 * Implementation of [ClientRateLimitPlugin] ensuring not more than
 * [ConcurrentRequestLimiterConfiguration.requestLimit] are being made concurrently.
 */
class ConcurrentRequestLimiter(private val client: HttpClient, private val requestLimit: Int) {
    private val inFlightRequests = AtomicInteger(0)
    private val requestQueue = ConcurrentLinkedQueue<HttpRequestBuilder>()

    /**
     * Cancels the request and adds it to the [requestQueue] to be re-requested when a response
     * comes back.
     *
     * <p>Called on [io.ktor.client.plugins.api.ClientPluginBuilder.onRequest].
     */
    fun onNewRequest(request: HttpRequestBuilder) {
        Napier.v("ConcurrentRequestLimiter#OnNewRequest()")
        if (inFlightRequests.get() >= requestLimit) {
            Napier.d("Request limit met[limit=$requestLimit]: queueing request")
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
        Napier.v("ConcurrentRequestLimiter#OnNewResponse()")
        if (inFlightRequests.decrementAndGet() < requestLimit && requestQueue.isNotEmpty()) {
            client.request(requestQueue.remove())
            Napier.d("Sent request from RequestQueue[empty=${requestQueue.isNotEmpty()}}]")
        }
    }
}

/** Configuration for [ConcurrentRequestLimiter]. */
class ConcurrentRequestLimiterConfiguration {
    /** The limit to the number of requests that should be made concurrently, defaults to 10. */
    var requestLimit: Int = 10
}
