package org.audux.bgg.request

import com.google.common.truth.Truth.assertThat
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.BggRequestException
import org.audux.bgg.common.ThingType
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.test.KoinTest

/** Unit test for [things] extension function. */
class ThingsRequestTest : KoinTest {
    private lateinit var client: BggClient

    @BeforeEach
    fun beforeEach() {
        client = TestUtils().setupEngineAndRequest("thing?id=1,2,3")
    }

    @Test
    fun `Makes a request with minimal parameters`() {
        runBlocking {
            val response = client.things(ids = arrayOf(1, 2, 3)).call()

            val engine = client.engine() as MockEngine
            val request = engine.requestHistory[0]
            assertThat(engine.requestHistory).hasSize(1)
            assertThat(request.method).isEqualTo(HttpMethod.Get)
            assertThat(request.headers)
                .isEqualTo(
                    Headers.build {
                        appendAll("Accept", listOf("*/*"))
                        appendAll("Accept-Charset", listOf("UTF-8"))
                    }
                )
            assertThat(request.url)
                .isEqualTo(
                    URLBuilder(
                            protocol = URLProtocol.HTTPS,
                            host = "boardgamegeek.com",
                            pathSegments = listOf("xmlapi2", "thing"),
                            parameters = Parameters.build { append("id", "1,2,3") }
                        )
                        .build()
                )
            assertThat(response.things).hasSize(3)
        }
    }

    @Test
    fun `Makes a request with all parameters`() {
        runBlocking {
            val response =
                client
                    .things(
                        ids = arrayOf(1, 2, 3),
                        types = arrayOf(ThingType.BOARD_GAME, ThingType.RPG_ITEM),
                        stats = true,
                        versions = true,
                        videos = true,
                        marketplace = true,
                        comments = true,
                        page = 1,
                        pageSize = 100,
                    )
                    .call()

            val engine = client.engine() as MockEngine
            val request = engine.requestHistory[0]
            assertThat(engine.requestHistory).hasSize(1)
            assertThat(request.url)
                .isEqualTo(
                    URLBuilder(
                            protocol = URLProtocol.HTTPS,
                            host = "boardgamegeek.com",
                            pathSegments = listOf("xmlapi2", "thing"),
                            parameters =
                                Parameters.build {
                                    append("id", "1,2,3")
                                    append("type", "boardgame,rpgitem")
                                    append("stats", "1")
                                    append("versions", "1")
                                    append("videos", "1")
                                    append("marketplace", "1")
                                    append("comments", "1")
                                    append("page", "1")
                                    append("pagesize", "100")
                                }
                        )
                        .build()
                )
            assertThat(response.things).hasSize(3)
        }
    }

    @Test
    fun `Throws when pageSize is too large`() {
        runBlocking {
            val exception =
                assertThrows<BggRequestException> {
                    client.things(ids = arrayOf(1), pageSize = 10_000).call()
                }
            assertThat(exception).hasMessageThat().isEqualTo("pageSize must be between 10 and 100")
        }
    }

    @Test
    fun `Throws when competing parameters are set`() {
        runBlocking {
            val exception =
                assertThrows<BggRequestException> {
                    client.things(ids = arrayOf(1), comments = true, ratingComments = true).call()
                }
            assertThat(exception)
                .hasMessageThat()
                .isEqualTo("comments and ratingsComments can't both be true")
        }
    }
}
