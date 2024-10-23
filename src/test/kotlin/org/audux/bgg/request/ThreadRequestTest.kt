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
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test

/** Unit tests for [thread] extension function. */
class ThreadRequestTest {
    @Test
    fun `Makes a request with wrong thread id`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("thread?id=0")
            BggClient.engine = { engine }

            val response = BggClient.thread(id = 0).call()

            val request = engine.requestHistory[0]
            assertThat(engine.requestHistory).hasSize(1)
            assertThat(request.method).isEqualTo(HttpMethod.Get)
            assertThat(request.headers).isEqualTo(TestUtils.DEFAULT_HEADERS)
            assertThat(request.url).isEqualTo(Url("https://boardgamegeek.com/xmlapi2/thread?id=0"))
            assertThat(response.isError()).isTrue()
            assertThat(response.isSuccess()).isFalse()
            assertThat(response.error)
                .isEqualTo(
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<error message='Thread Not Found' />"
                )
            assertThat(response.data).isNull()
        }
    }

    @Test
    fun `Makes a request with minimum parameters`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("thread")
            BggClient.engine = { engine }

            val response = BggClient.thread(id = 3208373).call()

            val request = engine.requestHistory[0]
            assertThat(engine.requestHistory).hasSize(1)
            assertThat(request.method).isEqualTo(HttpMethod.Get)
            assertThat(request.headers).isEqualTo(TestUtils.DEFAULT_HEADERS)
            assertThat(request.url)
                .isEqualTo(Url("https://boardgamegeek.com/xmlapi2/thread?id=3208373"))
            assertThat(response.isError()).isFalse()
            assertThat(response.isSuccess()).isTrue()
            assertThat(response.data?.articles).hasSize(13)
        }
    }

    @Test
    fun `Makes a request with all parameters`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("thread")
            BggClient.engine = { engine }

            val response =
                BggClient.thread(
                        id = 3208373,
                        minArticleId = 10,
                        minArticleDate = LocalDateTime.of(2020, 1, 1, 0, 0),
                        count = 100_000,
                    )
                    .call()

            val request = engine.requestHistory[0]
            assertThat(request.url)
                .isEqualTo(
                    Url(
                        "https://boardgamegeek.com/xmlapi2/thread?id=3208373&minarticleid=10&minarticledate=2020-01-01+00%3A00%3A00&count=100000"
                    )
                )
            assertThat(response.data?.articles).hasSize(13)
        }
    }
}
