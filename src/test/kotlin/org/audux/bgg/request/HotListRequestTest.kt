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
package org.audux.bgg.request

import com.google.common.truth.Truth.assertThat
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.common.HotListType
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test

/** Unit tests for [hotList] extension function. */
class HotListRequestTest {
    @Test
    fun `Makes a request with minimal parameters`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("hot")
            BggClient.engine = { engine }

            val response = BggClient.hotList().call()

            val request = engine.requestHistory[0]
            assertThat(engine.requestHistory).hasSize(1)
            assertThat(request.method).isEqualTo(HttpMethod.Get)
            assertThat(request.headers).isEqualTo(TestUtils.DEFAULT_HEADERS)
            assertThat(request.url).isEqualTo(Url("https://boardgamegeek.com/xmlapi2/hot"))
            assertThat(response.isError()).isFalse()
            assertThat(response.isSuccess()).isTrue()
            assertThat(response.data?.results).hasSize(50)
        }
    }

    @Test
    fun `Makes a request with all parameters`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("hot")
            BggClient.engine = { engine }

            val response = BggClient.hotList(HotListType.BOARD_GAME).call()

            val request = engine.requestHistory[0]
            assertThat(engine.requestHistory).hasSize(1)
            assertThat(request.method).isEqualTo(HttpMethod.Get)
            assertThat(request.headers).isEqualTo(TestUtils.DEFAULT_HEADERS)
            assertThat(request.url).isEqualTo(Url("https://boardgamegeek.com/xmlapi2/hot?type=boardgame"))
            assertThat(response.isError()).isFalse()
            assertThat(response.isSuccess()).isTrue()
            assertThat(response.data?.results).hasSize(50)
        }
    }


    @Test
    fun `Issue 31 - Handles malformed input`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("hot?type=boardgame", headers = Headers.build {
                append("content-encoding", "gzip")
                append("content-type", "text/xml; charset=\"UTF-8\"")
            })
            BggClient.engine = { engine }

            val response = BggClient.hotList(HotListType.BOARD_GAME).call()

            val request = engine.requestHistory[0]
            assertThat(engine.requestHistory).hasSize(1)
            assertThat(request.method).isEqualTo(HttpMethod.Get)
            assertThat(request.headers).isEqualTo(TestUtils.DEFAULT_HEADERS)
            assertThat(request.url)
                .isEqualTo(Url("https://boardgamegeek.com/xmlapi2/hot?type=boardgame"))

            assertThat(response.isError()).isFalse()
            assertThat(response.isSuccess()).isTrue()
            assertThat(response.data?.results).hasSize(50)
        }
    }
}
