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
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.common.PlayThingType
import org.audux.bgg.common.SubType
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** Unit tests for [plays] extension function. */
class PlaysRequestTest {

    @Test
    fun `Makes a request with invalid username`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("plays?username=userdoesnotexist")
            BggClient.engine = { engine }

            val response = BggClient.plays(username = "userdoesnotexist").call()

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
                .isEqualTo(Url("https://boardgamegeek.com/xmlapi2/plays?username=userdoesnotexist"))
            assertThat(response.isError()).isTrue()
            assertThat(response.isSuccess()).isFalse()
            assertThat(response.error)
                .isEqualTo(
                    "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
                        "<div class='messagebox error'>\n" +
                        "    Invalid object or user\n" +
                        "</div>"
                )
            assertThat(response.data).isNull()
        }
    }

    @Test
    fun `Makes a request with minimum parameters`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("plays?username=Novaeux")
            BggClient.engine = { engine }

            val response = BggClient.plays(username = "Novaeux").call()

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
                .isEqualTo(Url("https://boardgamegeek.com/xmlapi2/plays?username=Novaeux"))
            assertThat(response.isError()).isFalse()
            assertThat(response.isSuccess()).isTrue()
            assertThat(response.data?.plays).hasSize(6)
        }
    }

    @Test
    fun `Makes a request with all parameters`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("plays?username=Novaeux")
            BggClient.engine = { engine }

            val response =
                BggClient.plays(
                        username = "Novaeux",
                        page = 1,
                        subType = SubType.BOARD_GAME,
                        type = PlayThingType.THING,
                        minDate = LocalDate.of(2018, 2, 2),
                        maxDate = LocalDate.of(2020, 2, 7),
                    )
                    .call()

            val request = engine.requestHistory[0]
            assertThat(request.url)
                .isEqualTo(
                    Url(
                        "https://boardgamegeek.com/xmlapi2/plays?username=Novaeux&type=thing&mindate=2018-02-02&maxdate=2020-02-07&subtype=boardgame&page=1"
                    )
                )
            assertThat(response.data?.plays).hasSize(6)
        }
    }

    @Nested
    inner class Paginates {
        @Test
        fun `Automatically to the end`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "plays?username=auser&page=1",
                    "plays?username=auser&page=2",
                    "plays?username=auser&page=3",
                )
            BggClient.engine = { engine }

            val response = BggClient.plays(username = "auser").paginate().call()

            assertThat(engine.requestHistory).hasSize(3)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/plays?username=auser"),
                    Url("https://boardgamegeek.com/xmlapi2/plays?username=auser&page=2"),
                    Url("https://boardgamegeek.com/xmlapi2/plays?username=auser&page=3"),
                )
            assertThat(response.data?.total).isEqualTo(270)
            assertThat(response.data?.plays).hasSize(270)
        }

        @Test
        fun `To the toPage parameter`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "plays?username=auser&page=1",
                    "plays?username=auser&page=2",
                    "plays?username=auser&page=3",
                )
            BggClient.engine = { engine }

            val response = BggClient.plays(username = "auser").paginate(toPage = 2).call()

            assertThat(engine.requestHistory).hasSize(2)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/plays?username=auser"),
                    Url("https://boardgamegeek.com/xmlapi2/plays?username=auser&page=2"),
                )
            assertThat(response.data?.total).isEqualTo(270)
            assertThat(response.data?.plays).hasSize(200)
        }

        @Test
        fun `From the initial page to the end`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "plays?username=auser&page=2",
                    "plays?username=auser&page=3",
                )
            BggClient.engine = { engine }

            val response = BggClient.plays(username = "auser", page = 2).paginate().call()

            assertThat(engine.requestHistory).hasSize(2)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/plays?username=auser&page=2"),
                    Url("https://boardgamegeek.com/xmlapi2/plays?username=auser&page=3"),
                )
            assertThat(response.data?.total).isEqualTo(270)
            assertThat(response.data?.plays).hasSize(170)
        }

        @Test
        fun `Quietly skips failures`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "plays?username=auser&page=1",
                    "plays?username=userdoesnotexist", // Erroneous response
                    "plays?username=auser&page=3",
                )

            BggClient.engine = { engine }

            val response = BggClient.plays(username = "auser").paginate().call()

            assertThat(engine.requestHistory).hasSize(3)
            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/xmlapi2/plays?username=auser"),
                    Url("https://boardgamegeek.com/xmlapi2/plays?username=auser&page=2"),
                    Url("https://boardgamegeek.com/xmlapi2/plays?username=auser&page=3"),
                )
            assertThat(response.data?.total).isEqualTo(270)
            assertThat(response.data?.plays).hasSize(170)
        }
    }
}
