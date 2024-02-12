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
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.BggRequestException
import org.audux.bgg.common.ThingType
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** Unit test for [things] extension function. */
class ThingsRequestTest {
    @Test
    fun `Makes a request with minimal parameters`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("thing?id=1,2,3")
            BggClient.engine = { engine }

            val response = BggClient.things(ids = arrayOf(1, 2, 3)).call()

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
            assertThat(response.isError()).isFalse()
            assertThat(response.isSuccess()).isTrue()
            assertThat(response.data?.things).hasSize(3)
        }
    }

    @Test
    fun `Makes a request with all parameters`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("thing?id=1,2,3")
            BggClient.engine = { engine }

            val response =
                BggClient.things(
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
            assertThat(response.data?.things).hasSize(3)
        }
    }

    @Test
    fun `Throws when pageSize is too large`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("thing?id=1,2,3")
            BggClient.engine = { engine }

            val exception =
                assertThrows<BggRequestException> {
                    BggClient.things(ids = arrayOf(1), pageSize = 10_000).call()
                }
            assertThat(exception).hasMessageThat().isEqualTo("pageSize must be between 10 and 100")
        }
    }

    @Test
    fun `Throws when competing parameters are set`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("thing?id=1,2,3")
            BggClient.engine = { engine }

            val exception =
                assertThrows<BggRequestException> {
                    BggClient.things(ids = arrayOf(1), comments = true, ratingComments = true)
                        .call()
                }
            assertThat(exception)
                .hasMessageThat()
                .isEqualTo("comments and ratingsComments can't both be true")
        }
    }
}
