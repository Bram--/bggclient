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
import org.audux.bgg.InternalBggClient
import org.audux.bgg.common.Inclusion
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test

/** Unit tests for [guilds] extension function. */
class GuildsRequestTest {
    @Test
    fun `Makes a request with wrong guild id`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("guilds?id=-1")
            val client = InternalBggClient { engine }

            val response = client.guilds(id = -1).call()

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
            assertThat(request.url).isEqualTo(Url("https://boardgamegeek.com/xmlapi2/guilds?id=-1"))
            assertThat(response.isError()).isTrue()
            assertThat(response.isSuccess()).isFalse()
            assertThat(response.error)
                .isEqualTo(
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<guild id=\"-1\"  termsofuse=\"https://boardgamegeek.com/xmlapi/termsofuse\">\n" +
                        "    <error>Guild not found.</error>\n" +
                        "</guild>\n"
                )
            assertThat(response.data).isNull()
        }
    }

    @Test
    fun `Makes a request with minimum parameters`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("guilds?id=2310")
            val client = InternalBggClient { engine }

            val response = client.guilds(id = 2310).call()

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
                .isEqualTo(Url("https://boardgamegeek.com/xmlapi2/guilds?id=2310"))
            assertThat(response.isError()).isFalse()
            assertThat(response.isSuccess()).isTrue()
            assertThat(response.data?.name).isEqualTo("St Albans Board Games Club")
        }
    }

    @Test
    fun `Makes a request with all parameters`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("guilds?id=2310")
            val client = InternalBggClient { engine }

            val response =
                client
                    .guilds(
                        id = 2310,
                        members = Inclusion.INCLUDE,
                        sort = "date",
                        page = 1,
                    )
                    .call()

            val request = engine.requestHistory[0]
            assertThat(request.url)
                .isEqualTo(
                    Url(
                        "https://boardgamegeek.com/xmlapi2/guilds?id=2310&members=1&sort=date&page=1"
                    )
                )
            assertThat(response.data?.name).isEqualTo("St Albans Board Games Club")
        }
    }
}
