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
import org.audux.bgg.common.FamilyType
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test

/** Unit tests for [familyItems] extension function. */
class FamilyRequestTest {
    @Test
    fun `Makes a request with minimum parameters`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("family")
            val client = InternalBggClient { engine }

            val response = client.familyItems(ids = arrayOf(50152)).call()

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
                .isEqualTo(Url("https://boardgamegeek.com/xmlapi2/family?id=50152"))
            assertThat(response.isError()).isFalse()
            assertThat(response.isSuccess()).isTrue()
            assertThat(response.data?.items).hasSize(1)
            assertThat(response.data!!.items[0].links).hasSize(26)
        }
    }

    @Test
    fun `Makes a request with all parameters`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("family")
            val client = InternalBggClient { engine }

            val response =
                client
                    .familyItems(
                        ids = arrayOf(50152, 50153),
                        arrayOf(FamilyType.BOARD_GAME_FAMILY, FamilyType.RPG)
                    )
                    .call()

            val request = engine.requestHistory[0]
            assertThat(request.url)
                .isEqualTo(
                    Url(
                        "https://boardgamegeek.com/xmlapi2/family?id=50152%2C50153&type=boardgamefamily%2Crpg"
                    )
                )
            assertThat(response.data?.items).hasSize(1)
            assertThat(response.data!!.items[0].links).hasSize(26)
        }
    }
}
