package org.audux.bgg.request

import com.google.common.truth.Truth.assertThat
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.audux.bgg.common.HotListType
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest

/** Unit tests for [hot] extension function. */
class HotRequestTest : KoinTest {
    @Test
    fun `Makes a request with all parameters`() {
        runBlocking {
            val client = TestUtils().setupEngineAndRequest("hot")

            val response = client.hot(HotListType.BOARD_GAME).call()

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
                .isEqualTo(Url("https://boardgamegeek.com/xmlapi2/hot?type=boardgame"))
            assertThat(response.results).hasSize(50)
        }
    }
}
