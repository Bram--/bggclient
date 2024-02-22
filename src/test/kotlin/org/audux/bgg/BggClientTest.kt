package org.audux.bgg

import co.touchlab.kermit.Severity
import com.google.common.truth.Truth.assertThat
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.runBlocking
import org.audux.bgg.response.Response
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class BggClientTest {
    @ParameterizedTest
    @ValueSource(ints = [202, 429, 500, 599])
    fun `Retries on Http status codes`(statusCode: Int) {
        val engine =
            MockEngine(
                MockEngineConfig().apply {
                    addHandler { respond("Try later", HttpStatusCode.fromValue(statusCode)) }
                    addHandler { respondOk("Try later") }
                }
            )
        BggClient.engine = { engine }
        BggClient.configure { testRetryConfiguration(this) }

        runBlocking { BggClient.user(name = "Novaeux").call() }

        assertThat(engine.requestHistory).hasSize(2)
    }

    @Test
    fun `Retries config maxRetries times`() {
        val engine =
            MockEngine(
                MockEngineConfig().apply {
                    repeat(5) { addHandler { respond("Try later", HttpStatusCode.Accepted) } }
                }
            )
        BggClient.engine = { engine }
        BggClient.configure {
            testRetryConfiguration(this)
            maxRetries = 3
        }

        runBlocking { BggClient.user(name = "Novaeux").call() }

        assertThat(engine.requestHistory).hasSize(4) // Initial try + 3 retries.
    }

    @Test
    fun `Logs URL on retry`() {
        BggClient.engine = {
            MockEngine(
                MockEngineConfig().apply {
                    addHandler { respond("Try later", HttpStatusCode.Accepted) }
                    addHandler { respondOk("Try later") }
                }
            )
        }
        val logWrites = TestUtils.captureLoggerWrites(BggClient.Severity.Info)

        runBlocking { BggClient.user(name = "Novaeux").call() }

        assertThat(logWrites.logsWritten().size).isAtLeast(1)
        assertThat(logWrites.logsWritten())
            .contains(
                TestUtils.LogWrite(
                    severity = Severity.Info,
                    message =
                        "Got status code 202 Retrying request[https://boardgamegeek.com/xmlapi2/user?name=Novaeux",
                    tag = "HttpRequestRetry",
                    throwable = null
                )
            )
    }

    @Test
    fun `callAsync executes a request and invokes callback with response object`() {
        BggClient.engine = {
            MockEngine(MockEngineConfig().apply { addHandler { respondOk("Response") } })
        }
        val latch = CountDownLatch(1)
        var response: String? = null

        BggClient.InternalBggClient().apply {
            request {
                    Response(
                        data = client().get("https://www.google.com/test").bodyAsText(),
                        error = null
                    )
                }
                .callAsync() {
                    latch.countDown()
                    response = it.data
                }
        }

        latch.await()
        assertThat(latch.count).isEqualTo(0)
        assertThat(response).isEqualTo("Response")
    }

    @Test
    fun `callAsync returns a CompletableFuture for usage in Java`() {
        BggClient.engine = {
            MockEngine(MockEngineConfig().apply { addHandler { respondOk("Response") } })
        }
        var response: String?
        var future: CompletableFuture<Response<String>>? = null

        BggClient.InternalBggClient().apply {
            future =
                request {
                        Response(
                            data = client().get("https://www.google.com/test").bodyAsText(),
                            error = null
                        )
                    }
                    .callAsync()
        }

        runBlocking { response = future!!.get().data }

        assertThat(response).isEqualTo("Response")
    }

    @Test
    fun `call immediately executes the request`() {
        BggClient.engine = {
            MockEngine(MockEngineConfig().apply { addHandler { respondOk("Response") } })
        }
        var response: String?

        runBlocking {
            BggClient.InternalBggClient().apply {
                response =
                    request {
                            Response(
                                data = client().get("https://www.google.com/test").bodyAsText(),
                                error = null
                            )
                        }
                        .call()
                        .data
            }
        }

        assertThat(response).isEqualTo("Response")
    }

    private fun testRetryConfiguration(config: BggClientConfiguration) =
        config.apply {
            retryBase = 1.0
            retryMaxDelayMs = 1_000
            retryRandomizationMs = 1
        }
}
