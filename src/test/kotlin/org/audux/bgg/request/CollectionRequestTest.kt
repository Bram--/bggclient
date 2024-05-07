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
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.common.Inclusion
import org.audux.bgg.common.ThingType
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test

/** Unit tests for [collection] extension function. */
class CollectionRequestTest {
    @Test
    fun `Makes a request with a user that does not exist`() {
        runBlocking {
            val engine = TestUtils.setupMockEngine("collection?username=userdoesnotexist")
            BggClient.engine = { engine }

            val response =
                BggClient.collection(userName = "userdoesnotexist", subType = ThingType.RPG_ITEM)
                    .call()

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
                    Url(
                        "https://boardgamegeek.com/xmlapi2/collection?username=userdoesnotexist&subtype=rpgitem"
                    )
                )
            assertThat(response.isError()).isTrue()
            assertThat(response.isSuccess()).isFalse()
            assertThat(response.error)
                .isEqualTo(
                    "<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"yes\" ?>\n" +
                        "<errors>\n" +
                        "    <error>\n" +
                        "        <message>Invalid username specified</message>\n" +
                        "    </error>\n" +
                        "</errors>"
                )
            assertThat(response.data).isNull()
        }
    }

    @Test
    fun `Makes a request with minimum parameters`() {
        runBlocking {
            val engine =
                TestUtils.setupMockEngine("collection?username=novaeux&stats=1&subtype=rpgitem")
            BggClient.engine = { engine }

            val response = BggClient.collection(userName = "Noveaux").call()

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
                .isEqualTo(Url("https://boardgamegeek.com/xmlapi2/collection?username=Noveaux"))
            assertThat(response.isError()).isFalse()
            assertThat(response.isSuccess()).isTrue()
            assertThat(response.data!!.items).hasSize(1)
        }
    }

    @Test
    fun `Makes a request with all parameters`() {
        runBlocking {
            val engine =
                TestUtils.setupMockEngine(
                    "collection?username=novaeux&stats=1&subtype=boardgame&excludesubtype=boardgameexpansion"
                )
            BggClient.engine = { engine }

            val response =
                BggClient.collection(
                        userName = "Noveaux",
                        subType = ThingType.BOARD_GAME,
                        excludeSubType = ThingType.BOARD_GAME_EXPANSION,
                        ids = arrayOf(1, 2, 3),
                        version = true,
                        brief = true,
                        stats = true,
                        own = Inclusion.INCLUDE,
                        rated = Inclusion.INCLUDE,
                        played = Inclusion.INCLUDE,
                        comment = Inclusion.INCLUDE,
                        trade = Inclusion.INCLUDE,
                        want = Inclusion.INCLUDE,
                        wishlist = Inclusion.INCLUDE,
                        wishlistPriority = 5,
                        preOrdered = Inclusion.INCLUDE,
                        wantToPlay = Inclusion.INCLUDE,
                        wantToBuy = Inclusion.INCLUDE,
                        previouslyOwned = Inclusion.INCLUDE,
                        hasParts = Inclusion.INCLUDE,
                        wantParts = Inclusion.INCLUDE,
                        minRating = 1,
                        rating = 4,
                        minBggRating = 2,
                        minimumPlays = 1,
                        maxPlays = 99,
                        collectionId = -1,
                        modifiedSince = LocalDateTime.of(2020, 1, 1, 0, 0),
                    )
                    .call()

            val request = engine.requestHistory[0]
            assertThat(engine.requestHistory).hasSize(1)
            val expectedUrl =
                URLBuilder(
                        protocol = URLProtocol.HTTPS,
                        host = "boardgamegeek.com",
                        pathSegments = listOf("xmlapi2", "collection"),
                        parameters =
                            Parameters.build {
                                append("username", "Noveaux")
                                append("subtype", ThingType.BOARD_GAME.param)
                                append("excludesubtype", ThingType.BOARD_GAME_EXPANSION.param)
                                append("id", arrayOf(1, 2, 3).joinToString(","))
                                append("version", "1")
                                append("brief", "1")
                                append("stats", "1")
                                append("own", "1")
                                append("rated", "1")
                                append("played", "1")
                                append("comment", "1")
                                append("trade", "1")
                                append("want", "1")
                                append("wishlist", "1")
                                append("wishlistpriority", "5")
                                append("preordered", "1")
                                append("wanttoplay", "1")
                                append("wanttobuy", "1")
                                append("prevowned", "1")
                                append("hasparts", "1")
                                append("wantparts", "1")
                                append("minrating", "1")
                                append("rating", "4")
                                append("minbggrating", "2")
                                append("minplays", "1")
                                append("maxplays", "99")
                                append("collid", "-1")
                                append("modifiedsince", "2020-01-01 00:00:00")
                            }
                    )
                    .build()
            assertThat(request.url).isEqualTo(expectedUrl)
            assertThat(response.data!!.items).hasSize(105)
        }
    }
}
