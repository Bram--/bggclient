package org.audux.bgg

import co.touchlab.kermit.Severity
import com.google.common.truth.Truth.assertThat
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.respond
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.plugins.HttpRequestTimeoutException
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CountDownLatch
import kotlinx.coroutines.runBlocking
import org.audux.bgg.response.Response
import org.audux.bgg.util.TestUtils
import org.audux.bgg.util.TestUtils.delayedResponse
import org.audux.bgg.util.TestUtils.setupMockEngine
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class BggClientTest {
    private lateinit var defaultConfiguration: BggClientConfiguration

    @BeforeEach
    fun setUp() {
        defaultConfiguration = BggClient.configuration
    }

    @AfterEach
    fun tearDown() {
        BggClient.configuration = defaultConfiguration
    }

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
                    throwable = null,
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

        InstantiableClient().apply {
            request {
                    Response(
                        data = client().get("https://www.google.com/test").bodyAsText(),
                        error = null,
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

        InstantiableClient().apply {
            future =
                request {
                        Response(
                            data = client().get("https://www.google.com/test").bodyAsText(),
                            error = null,
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
            InstantiableClient().apply {
                response =
                    request {
                            Response(
                                data = client().get("https://www.google.com/test").bodyAsText(),
                                error = null,
                            )
                        }
                        .call()
                        .data
            }
        }

        assertThat(response).isEqualTo("Response")
    }

    @Test
    fun `Throws Exception when request timeout has been met`() {
        runBlocking {
            BggClient.configure { this.requestTimeoutMillis = 10 }
            BggClient.engine = {
                MockEngine(MockEngineConfig().apply { addHandler(delayedResponse(1_000)) })
            }

            InstantiableClient().apply {
                assertThrows<HttpRequestTimeoutException> {
                    request {
                            Response(
                                data = client().get("https://www.google.com/test").bodyAsText(),
                                error = null,
                            )
                        }
                        .call()
                }
            }
        }
    }

    @Nested
    inner class UnknownProperty {
        @Test
        fun `fails parsing an unknown property`() = runBlocking {
            BggClient.configure { failOnUnknownProperties = true }
            BggClient.engine = { setupMockEngine("thing?id=with-unknown-property") }

            val response = BggClient.things(ids = arrayOf(1)).call()

            assertThat(response.data).isNull()
            assertThat(response.error)
                .isEqualTo(
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<items termsofuse=\"https://boardgamegeek.com/xmlapi/termsofuse\">\n" +
                        "    <item><unkown>Exception</unkown></item>\n" +
                        "</items>"
                )

            BggClient.configure { failOnUnknownProperties = true }
        }

        @Test
        fun `parses an item wih an unknown field`() = runBlocking {
            BggClient.configure { failOnUnknownProperties = false }
            BggClient.engine = { setupMockEngine("thing?id=with-unknown-property") }

            val response = BggClient.things(ids = arrayOf(1)).call()

            assertThat(response.data).isNotNull()
            assertThat(response.error).isNull()

            BggClient.configure { failOnUnknownProperties = true }
        }
    }

    private fun testRetryConfiguration(config: BggClientConfiguration) =
        config.apply {
            retryBase = 1.0
            retryMaxDelayMs = 1_000
            retryRandomizationMs = 1
        }
}
