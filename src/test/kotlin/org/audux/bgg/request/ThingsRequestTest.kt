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
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.BggRequestException
import org.audux.bgg.common.ThingType
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Nested
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
                .isEqualTo(TestUtils.DEFAULT_HEADERS)
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
                        page = 2,
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
                                    append("page", "2")
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

    @Nested
    inner class Paginates {
        @Test
        fun `Automatically to the end`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "thing?id=396790&comments=1&page=1",
                    "thing?id=396790&comments=1&page=2",
                    "thing?id=396790&comments=1&page=3",
                )
            BggClient.engine = { engine }

            val response =
                BggClient.things(ids = arrayOf(396790), comments = true).paginate().call()

            assertThat(engine.requestHistory).hasSize(3)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/thing?id=396790&comments=1"),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/thing?id=396790&comments=1&page=2&pagesize=100"
                    ),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/thing?id=396790&comments=1&page=3&pagesize=100"
                    ),
                )
            assertThat(response.data?.things).hasSize(1)
            assertThat(response.data?.things!![0].names).hasSize(3)
            assertThat(response.data?.things!![0].links).hasSize(35)
            assertThat(response.data?.things!![0].polls).hasSize(3)
            assertThat(response.data?.things!![0].comments?.totalItems).isEqualTo(213)
            assertThat(response.data?.things!![0].comments?.comments).hasSize(213)
        }

        @Test
        fun `To the toPage parameter`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "thing?id=396790&comments=1&page=1",
                    "thing?id=396790&comments=1&page=2",
                    "thing?id=396790&comments=1&page=3",
                )
            BggClient.engine = { engine }

            val response =
                BggClient.things(ids = arrayOf(396790), comments = true).paginate(toPage = 2).call()

            assertThat(engine.requestHistory).hasSize(2)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/thing?id=396790&comments=1"),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/thing?id=396790&comments=1&page=2&pagesize=100"
                    ),
                )
            assertThat(response.data?.things).hasSize(1)
            assertThat(response.data?.things!![0].comments?.totalItems).isEqualTo(213)
            assertThat(response.data?.things!![0].comments?.comments).hasSize(200)
        }

        @Test
        fun `From the initial page to the end`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "thing?id=396790&comments=1&page=2",
                    "thing?id=396790&comments=1&page=3",
                )
            BggClient.engine = { engine }

            val response =
                BggClient.things(ids = arrayOf(396790), ratingComments = true, page = 2)
                    .paginate()
                    .call()

            assertThat(engine.requestHistory).hasSize(2)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url(
                        "https://boardgamegeek.com/xmlapi2/thing?id=396790&ratingcomments=1&page=2"
                    ),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/thing?id=396790&ratingcomments=1&page=3&pagesize=100"
                    ),
                )
            assertThat(response.data?.things).hasSize(1)
            assertThat(response.data?.things!![0].comments?.totalItems).isEqualTo(213)
            assertThat(response.data?.things!![0].comments?.comments).hasSize(113)
        }

        @Test
        fun `Quietly skips empty responses`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "thing?id=396790&comments=1&page=1",
                    "thing?id=-1",
                    "thing?id=396790&page=3",
                )

            BggClient.engine = { engine }

            val response =
                BggClient.things(ids = arrayOf(396790), comments = true).paginate().call()

            assertThat(engine.requestHistory).hasSize(3)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/thing?id=396790&comments=1"),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/thing?id=396790&comments=1&page=2&pagesize=100"
                    ),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/thing?id=396790&comments=1&page=3&pagesize=100"
                    ),
                )
            assertThat(response.data?.things).hasSize(1)
            assertThat(response.data?.things!![0].comments?.totalItems).isEqualTo(213)
            assertThat(response.data?.things!![0].comments?.comments).hasSize(100)
        }

        @Test
        fun `Quietly skips failures`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "thing?id=396790&comments=1&page=1",
                    "thread",
                    "thing?id=396790&comments=1&page=3",
                )

            BggClient.engine = { engine }

            val response =
                BggClient.things(ids = arrayOf(396790), comments = true).paginate().call()

            assertThat(engine.requestHistory).hasSize(3)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/thing?id=396790&comments=1"),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/thing?id=396790&comments=1&page=2&pagesize=100"
                    ),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/thing?id=396790&comments=1&page=3&pagesize=100"
                    ),
                )
            assertThat(response.data?.things).hasSize(1)
            assertThat(response.data?.things!![0].comments?.totalItems).isEqualTo(213)
            assertThat(response.data?.things!![0].comments?.comments).hasSize(113)
        }

        @Test
        fun `Throws when comments AND ratings comments param is not set`() {
            runBlocking {
                val engine = TestUtils.setupMockEngine("thing?id=396790&comments=1&page=1")
                BggClient.engine = { engine }

                assertThrows(
                    "Nothing to paginate without the either the comments or ratingComments parameter set."
                ) {
                    BggClient.things(ids = arrayOf(396790)).paginate().call()
                }
                    as BggRequestException
            }
        }
    }
}
