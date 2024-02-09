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
import kotlinx.coroutines.runBlocking
import org.audux.bgg.common.Inclusion
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test
import org.koin.test.KoinTest

/** Unit tests for [geekList] extension function. */
class GeekListRequestTest : KoinTest {
    @Test
    fun `Makes a request with minimal parameters`() {
        runBlocking {
            val client = TestUtils.setupEngineAndRequest("geeklist?id=331520")

            val response = client.geekList(id = 331520).call()

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
                .isEqualTo(Url("https://boardgamegeek.com/xmlapi/geeklist/331520"))
            assertThat(response.items).hasSize(10)
        }
    }

    @Test
    fun `Makes a request with all parameters`() {
        runBlocking {
            val client = TestUtils.setupEngineAndRequest("geeklist?id=331520&comments=1")

            val response = client.geekList(id = 331520, comments = Inclusion.INCLUDE).call()

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
                .isEqualTo(Url("https://boardgamegeek.com/xmlapi/geeklist/331520?comments=1"))
            assertThat(response.items).hasSize(10)
        }
    }
}
