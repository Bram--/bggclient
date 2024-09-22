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
import org.audux.bgg.BggRequestException
import org.audux.bgg.common.Domain
import org.audux.bgg.common.Inclusion
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** Unit tests for [user] extension function. */
class UserRequestTest {
    @Test
    fun `Makes a request with minimum parameters`() {
        runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "user?name=Novaeux&buddies=1&hot=1&top=1&guilds=1&page=1&domain=boardgame"
                )
            BggClient.engine = { engine }

            val response = BggClient.user(name = "Novaeux").call()

            val request = engine.requestHistory[0]
            assertThat(engine.requestHistory).hasSize(1)
            assertThat(request.method).isEqualTo(HttpMethod.Get)
            assertThat(request.headers)
                .isEqualTo(TestUtils.DEFAULT_HEADERS)
            assertThat(request.url)
                .isEqualTo(Url("https://boardgamegeek.com/xmlapi2/user?name=Novaeux"))
            assertThat(response.isError()).isFalse()
            assertThat(response.isSuccess()).isTrue()
            assertThat(response.data?.name).isEqualTo("Novaeux")
        }
    }

    @Test
    fun `Makes a request with all parameters`() {
        runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "user?name=Novaeux&buddies=1&hot=1&top=1&guilds=1&page=1&domain=boardgame"
                )
            BggClient.engine = { engine }

            val response =
                BggClient.user(
                        name = "Novaeux",
                        buddies = Inclusion.INCLUDE,
                        guilds = Inclusion.INCLUDE,
                        top = Inclusion.INCLUDE,
                        hot = Inclusion.INCLUDE,
                        domain = Domain.BOARD_GAME_GEEK,
                        page = 1,
                    )
                    .call()

            val request = engine.requestHistory[0]
            assertThat(request.url)
                .isEqualTo(
                    Url(
                        "https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&guilds=1&top=1&hot=1&domain=boardgame&page=1"
                    )
                )
            assertThat(response.data?.name).isEqualTo("Novaeux")
        }
    }

    @Nested
    inner class Paginates {
        @Test
        fun `Automatically to the end`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "user?name=Novaeux&buddies=1&guilds=1&page=1",
                    "user?name=Novaeux&buddies=1&guilds=1&page=2",
                    "user?name=Novaeux&buddies=1&guilds=1&page=3",
                )
            BggClient.engine = { engine }

            val response =
                BggClient.user(
                        name = "Novaeux",
                        buddies = Inclusion.INCLUDE,
                        guilds = Inclusion.INCLUDE
                    )
                    .paginate()
                    .call()

            assertThat(engine.requestHistory).hasSize(3)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&guilds=1"),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&guilds=1&page=2"
                    ),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&guilds=1&page=3"
                    ),
                )
            assertThat(response.data?.buddies?.total).isEqualTo(2_200)
            assertThat(response.data?.buddies?.buddies).hasSize(2_200)
            assertThat(response.data?.guilds?.total).isEqualTo(1_900)
            assertThat(response.data?.guilds?.guilds).hasSize(1_900)
        }

        @Test
        fun `Automatically to the end - buddies only`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "user?name=Novaeux&buddies=1&page=1",
                    "user?name=Novaeux&buddies=1&page=2",
                    "user?name=Novaeux&buddies=1&page=3",
                )
            BggClient.engine = { engine }

            val response =
                BggClient.user(name = "Novaeux", buddies = Inclusion.INCLUDE).paginate().call()

            assertThat(engine.requestHistory).hasSize(3)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1"),
                    Url("https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&page=2"),
                    Url("https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&page=3"),
                )
            assertThat(response.data?.buddies?.total).isEqualTo(2_200)
            assertThat(response.data?.buddies?.buddies).hasSize(2_200)
            assertThat(response.data?.guilds).isNull()
        }

        @Test
        fun `Automatically to the end - guilds only`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "user?name=Novaeux&guilds=1&page=1",
                    "user?name=Novaeux&guilds=1&page=2",
                )
            BggClient.engine = { engine }

            val response =
                BggClient.user(name = "Novaeux", guilds = Inclusion.INCLUDE).paginate().call()

            assertThat(engine.requestHistory).hasSize(2)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/user?name=Novaeux&guilds=1"),
                    Url("https://boardgamegeek.com/xmlapi2/user?name=Novaeux&guilds=1&page=2"),
                )
            assertThat(response.data?.buddies).isNull()
            assertThat(response.data?.guilds?.total).isEqualTo(1_900)
            assertThat(response.data?.guilds?.guilds).hasSize(1_900)
        }

        @Test
        fun `To the toPage parameter`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "user?name=Novaeux&buddies=1&guilds=1&page=1",
                    "user?name=Novaeux&buddies=1&guilds=1&page=2",
                    "user?name=Novaeux&buddies=1&guilds=1&page=3",
                )
            BggClient.engine = { engine }

            val response =
                BggClient.user(
                        name = "Novaeux",
                        buddies = Inclusion.INCLUDE,
                        guilds = Inclusion.INCLUDE
                    )
                    .paginate(toPage = 2)
                    .call()

            assertThat(engine.requestHistory).hasSize(2)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&guilds=1"),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&guilds=1&page=2"
                    ),
                )
            assertThat(response.data?.buddies?.total).isEqualTo(2_200)
            assertThat(response.data?.buddies?.buddies).hasSize(2_000)
            assertThat(response.data?.guilds?.total).isEqualTo(1_900)
            assertThat(response.data?.guilds?.guilds).hasSize(1_900)
        }

        @Test
        fun `From the initial page to the end`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "user?name=Novaeux&buddies=1&guilds=1&page=2",
                    "user?name=Novaeux&buddies=1&guilds=1&page=3",
                )
            BggClient.engine = { engine }

            val response =
                BggClient.user(
                        name = "Novaeux",
                        buddies = Inclusion.INCLUDE,
                        guilds = Inclusion.INCLUDE,
                        page = 2
                    )
                    .paginate()
                    .call()

            assertThat(engine.requestHistory).hasSize(2)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url(
                        "https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&guilds=1&page=2"
                    ),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&guilds=1&page=3"
                    ),
                )
            assertThat(response.data?.buddies?.total).isEqualTo(2_200)
            assertThat(response.data?.buddies?.buddies).hasSize(1_200)
            assertThat(response.data?.guilds?.total).isEqualTo(1_900)
            assertThat(response.data?.guilds?.guilds).hasSize(900)
        }

        @Test
        fun `Quietly skips empty responses`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "user?name=Novaeux&buddies=1&guilds=1&page=1",
                    "user?name=NULL",
                    "user?name=Novaeux&buddies=1&guilds=1&page=3",
                )

            BggClient.engine = { engine }

            val response =
                BggClient.user(
                        name = "Novaeux",
                        buddies = Inclusion.INCLUDE,
                        guilds = Inclusion.INCLUDE
                    )
                    .paginate()
                    .call()

            assertThat(engine.requestHistory).hasSize(3)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&guilds=1"),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&guilds=1&page=2"
                    ),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&guilds=1&page=3"
                    ),
                )
            assertThat(response.data?.buddies?.total).isEqualTo(2_200)
            assertThat(response.data?.buddies?.buddies).hasSize(1_200)
            assertThat(response.data?.guilds?.total).isEqualTo(1_900)
            assertThat(response.data?.guilds?.guilds).hasSize(1000)
        }

        @Test
        fun `Quietly skips failures`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "user?name=Novaeux&buddies=1&guilds=1&page=1",
                    "thread",
                    "user?name=Novaeux&buddies=1&guilds=1&page=3",
                )

            BggClient.engine = { engine }

            val response =
                BggClient.user(
                        name = "Novaeux",
                        buddies = Inclusion.INCLUDE,
                        guilds = Inclusion.INCLUDE
                    )
                    .paginate()
                    .call()

            assertThat(engine.requestHistory).hasSize(3)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&guilds=1"),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&guilds=1&page=2"
                    ),
                    Url(
                        "https://boardgamegeek.com/xmlapi2/user?name=Novaeux&buddies=1&guilds=1&page=3"
                    ),
                )
            assertThat(response.data?.buddies?.total).isEqualTo(2_200)
            assertThat(response.data?.buddies?.buddies).hasSize(1_200)
            assertThat(response.data?.guilds?.total).isEqualTo(1_900)
            assertThat(response.data?.guilds?.guilds).hasSize(1000)
        }

        @Test
        fun `Throws when buddies AND guild param are not set`() {
            runBlocking {
                val engine =
                    TestUtils.setupMockEngine("user?name=Novaeux&buddies=1&guilds=1&page=1")
                BggClient.engine = { engine }

                assertThrows(
                    "Nothing to paginate without the either the comments or ratingComments parameter set."
                ) {
                    BggClient.user(name = "Novaeux").paginate().call()
                }
                    as BggRequestException
            }
        }
    }
}
