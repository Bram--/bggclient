package org.audux.bgg.request

import com.google.common.truth.Truth.assertThat
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.common.HotListType
import org.audux.bgg.module.BggHttpEngine
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest

class HotRequestTest : KoinTest {
    @Test
    fun `Makes a request with parameters`() {
        runBlocking {
            val mockEngine = MockEngine { respondOk(String(TestUtils.xml("hot").readAllBytes())) }
            val client = BggClient()
            client
                .getKoin()
                .loadModules(
                    listOf(
                        module {
                            single(named<BggHttpEngine>()) {
                                // Not useless as mockEngine needs to be bound to HttpClientEngine
                                // and not set up a new binding for HttpClientEngine
                                @Suppress("USELESS_CAST")
                                mockEngine as HttpClientEngine
                            }
                        }
                    )
                )
            val response = client.hot(HotListType.BOARD_GAME).call()

            val request = mockEngine.requestHistory[0]
            assertThat(mockEngine.requestHistory).hasSize(1)
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
            client.close()
        }
    }
}
