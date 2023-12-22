/**
 * Copyright 2023 Bram Wijnands
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
