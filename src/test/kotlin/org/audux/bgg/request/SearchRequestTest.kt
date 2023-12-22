package org.audux.bgg.request

import com.google.common.truth.Truth.assertThat
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.audux.bgg.common.ThingType
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest

/** Unit tests for [search] extension function. */
class SearchRequestTest : KoinTest {
    @Test
    fun `Makes a request with minimal parameters`() {
        runBlocking {
            val client = TestUtils().setupEngineAndRequest("search?query=my+little")
            val response = client.search(query = "my little").call()

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
                    Url("https://boardgamegeek.com/xmlapi2/search?query=my+little"),
                )
            assertThat(response.results).hasSize(144)
        }
    }

    @Test
    fun `Makes a request with all parameters`() {
        runBlocking {
            val client = TestUtils().setupEngineAndRequest("search?query=my+little")
            val response =
                client
                    .search(
                        query = "my little",
                        types = arrayOf(ThingType.BOARD_GAME, ThingType.RPG_ITEM),
                        exactMatch = true
                    )
                    .call()

            val engine = client.engine() as MockEngine
            val request = engine.requestHistory[0]
            assertThat(request.url)
                .isEqualTo(
                    Url(
                        "https://boardgamegeek.com/xmlapi2/search?query=my+little&type=boardgame%2Crpgitem&exact=1"
                    ),
                )
            assertThat(response.results).hasSize(144)
        }
    }
}
