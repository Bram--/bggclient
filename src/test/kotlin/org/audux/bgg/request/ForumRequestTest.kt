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
import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** Unit tests for [forum] extension function. */
class ForumRequestTest {
    @Test
    fun `Makes a request with wrong forum ID`() = runBlocking {
        val engine = TestUtils.setupMockEngine("forum?id=-1")
        BggClient.engine = { engine }

        val response = BggClient.forum(id = -1).call()

        val request = engine.requestHistory[0]
        assertThat(engine.requestHistory).hasSize(1)
        assertThat(request.method).isEqualTo(HttpMethod.Get)
        assertThat(request.headers).isEqualTo(TestUtils.DEFAULT_HEADERS)
        assertThat(request.url).isEqualTo(Url("https://boardgamegeek.com/xmlapi2/forum?id=-1"))
        assertThat(response.isError()).isTrue()
        assertThat(response.isSuccess()).isFalse()
        assertThat(response.error).hasLength(13_757)
        assertThat(response.data).isNull()
    }

    @Test
    fun `Makes a request with minimum parameters`() = runBlocking {
        val engine = TestUtils.setupMockEngine("forum?id=3696796")
        BggClient.engine = { engine }

        val response = BggClient.forum(id = 3696796).call()

        val request = engine.requestHistory[0]
        assertThat(engine.requestHistory).hasSize(1)
        assertThat(request.method).isEqualTo(HttpMethod.Get)
        assertThat(request.headers).isEqualTo(TestUtils.DEFAULT_HEADERS)
        assertThat(request.url).isEqualTo(Url("https://boardgamegeek.com/xmlapi2/forum?id=3696796"))
        assertThat(response.isError()).isFalse()
        assertThat(response.isSuccess()).isTrue()
        assertThat(response.data?.threads).hasSize(50)
    }

    @Test
    fun `Makes a request with all parameters`() = runBlocking {
        val engine = TestUtils.setupMockEngine("forum?id=3696796")
        BggClient.engine = { engine }

        val response = BggClient.forum(id = 3696796, page = 0).call()

        val request = engine.requestHistory[0]
        assertThat(request.url)
            .isEqualTo(Url("https://boardgamegeek.com/xmlapi2/forum?id=3696796&page=0"))
        assertThat(response.data!!.threads).hasSize(PaginatedForum.PAGE_SIZE)
    }

    @Nested
    inner class Paginates {
        @Test
        fun `Automatically to the end`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "forum?id=3696796",
                    "forum?id=3696796&page=2",
                    "forum?id=3696796&page=3",
                )

            BggClient.engine = { engine }

            val response = BggClient.forum(id = 3696796).paginate().call()

            assertThat(engine.requestHistory).hasSize(3)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/forum?id=3696796"),
                    Url("https://boardgamegeek.com/xmlapi2/forum?id=3696796&page=2"),
                    Url("https://boardgamegeek.com/xmlapi2/forum?id=3696796&page=3"),
                )
            assertThat(response.data!!.numThreads).isEqualTo(148)
            assertThat(response.data!!.threads).hasSize(148)
        }

        @Test
        fun `To the toPage parameter`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "forum?id=3696796",
                    "forum?id=3696796&page=2",
                    "forum?id=3696796&page=3",
                )
            BggClient.engine = { engine }

            val response = BggClient.forum(id = 3696796).paginate(toPage = 2).call()

            assertThat(engine.requestHistory).hasSize(2)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/forum?id=3696796"),
                    Url("https://boardgamegeek.com/xmlapi2/forum?id=3696796&page=2"),
                )
            assertThat(response.data!!.numThreads).isEqualTo(148)
            assertThat(response.data!!.threads).hasSize(100)
        }

        @Test
        fun `From the initial page to the end`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine("forum?id=3696796&page=2", "forum?id=3696796&page=3")
            BggClient.engine = { engine }

            val response = BggClient.forum(id = 3696796, page = 2).paginate().call()

            assertThat(engine.requestHistory).hasSize(2)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/forum?id=3696796&page=2"),
                    Url("https://boardgamegeek.com/xmlapi2/forum?id=3696796&page=3"),
                )
            assertThat(response.data!!.numThreads).isEqualTo(148)
            assertThat(response.data!!.threads).hasSize(98)
        }

        @Test
        fun `Quietly skips failures`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "forum?id=3696796",
                    "forum?id=-1", // Erroneous reply
                    "forum?id=3696796&page=3",
                )

            BggClient.engine = { engine }

            val response = BggClient.forum(id = 3696796).paginate().call()

            assertThat(engine.requestHistory).hasSize(3)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/forum?id=3696796"),
                    Url("https://boardgamegeek.com/xmlapi2/forum?id=3696796&page=2"),
                    Url("https://boardgamegeek.com/xmlapi2/forum?id=3696796&page=3"),
                )
            assertThat(response.data!!.numThreads).isEqualTo(148)
            assertThat(response.data!!.threads).hasSize(98)
        }
    }
}
