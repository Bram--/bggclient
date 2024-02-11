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
import io.ktor.client.engine.mock.MockEngine
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import java.time.LocalDate
import kotlinx.coroutines.runBlocking
import org.audux.bgg.common.PlayThingType
import org.audux.bgg.common.SubType
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest

/** Unit tests for [plays] extension function. */
class PlaysRequestTest : KoinTest {

    @Test
    fun `Makes a request with invalid username`() {
        runBlocking {
            val client = TestUtils.setupEngineAndRequest("plays?username=userdoesnotexist")

            val response = client.plays(username = "userdoesnotexist").call()

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
            val client = TestUtils.setupEngineAndRequest("plays?username=Novaeux")

            val response = client.plays(username = "Novaeux").call()

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
                .isEqualTo(Url("https://boardgamegeek.com/xmlapi2/plays?username=Novaeux"))
            assertThat(response.isError()).isFalse()
            assertThat(response.isSuccess()).isTrue()
            assertThat(response.data?.plays).hasSize(6)
        }
    }

    @Test
    fun `Makes a request with all parameters`() {
        runBlocking {
            val client = TestUtils.setupEngineAndRequest("plays?username=Novaeux")

            val response =
                client
                    .plays(
                        username = "Novaeux",
                        page = 1,
                        subType = SubType.BOARD_GAME,
                        type = PlayThingType.THING,
                        minDate = LocalDate.of(2018, 2, 2),
                        maxDate = LocalDate.of(2020, 2, 7),
                    )
                    .call()

            val engine = client.engine() as MockEngine
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
}
