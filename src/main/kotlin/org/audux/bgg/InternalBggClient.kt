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
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.compression.ContentEncoding
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.audux.bgg.plugin.ClientConcurrentRateLimitPlugin
import org.audux.bgg.plugin.ClientRateLimitPlugin
import org.audux.bgg.request.Request
import org.audux.bgg.response.Response

/** Internal BGG Client containing the actual implementations of the API Calls. */
internal class InternalBggClient {
    private val clientScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    internal val client = {
        HttpClient(BggClient.engine()) {
            // This plugin serves two primary purposes:
            //
            // * Sets the Accept-Encoding header with the specified quality value.
            // * Decodes content received from a server to obtain the original payload.
            install(ContentEncoding) { gzip() }

            // Limit the number of concurrent requests BGGClient makes at any time.
            install(ClientConcurrentRateLimitPlugin) {
                requestLimit = BggClient.configuration.maxConcurrentRequests
            }

            // Limit the number of request that are made per window e.g. (60 request per minute).
            install(ClientRateLimitPlugin) {
                requestLimit = BggClient.configuration.requestsPerWindowLimit
                windowSize = BggClient.configuration.requestWindowSize
            }

            // HttpTimeout handles the following behaviours:
            //
            // * Request timeout — a time period required to process an HTTP call: from sending a
            //  request to receiving a response.
            // * Connection timeout — a time period in which a client should establish a
            //  connection with a server.
            // * Socket timeout — a maximum time of inactivity between two data packets when
            //  exchanging data with a server.
            install(HttpTimeout) {
                requestTimeoutMillis = BggClient.configuration.requestTimeoutMillis
            }

            // Plugin to configure the retry policy for failed requests in various ways: specify
            // the number of retries, configure conditions for retrying a request, or modify a
            // request before retrying.
            install(HttpRequestRetry) {
                exponentialDelay(
                    base = BggClient.configuration.retryBase,
                    maxDelayMs = BggClient.configuration.retryMaxDelayMs,
                    randomizationMs = BggClient.configuration.retryRandomizationMs,
                )
                retryIf(maxRetries = BggClient.configuration.maxRetries) { request, response ->
                    response.status.value.let {
                        // Add 429 (TooManyRequests) and 202 (Accepted) for retries, see:
                        // https://boardgamegeek.com/thread/1188687/export-collections-has-been-updated-xmlapi-develop
                        (it in (500..599) + 202 + 429).also { shouldRetry ->
                            if (shouldRetry) {
                                Logger.i("HttpRequestRetry") {
                                    "Got status code $it Retrying request[${request.url}"
                                }
                            }
                        }
                    }
                }
            }

            expectSuccess = true
        }
    }

    val mapper: ObjectMapper =
        XmlMapper.builder()
            .apply {
                configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                configure(
                    DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
                    BggClient.configuration.failOnUnknownProperties,
                )

                addModule(JacksonXmlModule())
                addModule(JavaTimeModule())
                addModule(
                    KotlinModule.Builder()
                        .enable(KotlinFeature.NullToEmptyCollection)
                        .enable(KotlinFeature.StrictNullChecks)
                        .build()
                )

                // Keep hardcoded to US: https://bugs.openjdk.org/browse/JDK-8251317
                // en_GB Locale uses 'Sept' as a shortname when formatting dates (e.g. 'MMM').
                // The locale en_US remains 'Sep'.
                defaultLocale(Locale.US)
                defaultMergeable(true)
                defaultUseWrapper(false)
            }
            .build()

    /**
     * Calls/Launches a request async, once a response is available it will call [responseCallback].
     */
    internal fun <T> callAsync(request: suspend () -> T, responseCallback: (T) -> Unit) =
        clientScope.launch {
            val response = request()
            withContext(Dispatchers.Default) { responseCallback(response) }
        }

    /** Calls/Launches a request and returns it's response. */
    @OptIn(DelicateCoroutinesApi::class)
    internal fun <T> callAsync(request: suspend () -> Response<T>) =
        GlobalScope.future { request() }

    /** Calls/Launches a request and returns it's response. */
    internal suspend fun <T> call(request: suspend () -> Response<T>) = request()

    /** Returns a wrapped request for later execution. */
    internal fun <T> request(request: suspend () -> Response<T>) = Request(this, request)
}
