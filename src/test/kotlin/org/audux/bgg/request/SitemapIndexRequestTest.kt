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
import io.ktor.client.engine.mock.MockEngineConfig
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respondBadRequest
import io.ktor.client.engine.mock.respondOk
import io.ktor.client.request.HttpRequestData
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.Url
import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.common.Domain
import org.audux.bgg.common.SitemapLocationType
import org.audux.bgg.response.SitemapLocation
import org.audux.bgg.util.TestUtils
import org.audux.bgg.util.TestUtils.xml
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** Unit tests for [sitemapIndex] extension function. */
class SitemapIndexRequestTest {
    @Test
    fun `Makes a request to retrieve the sitemap index - boardgamegeek_com`() = runBlocking {
        val engine = TestUtils.setupMockEngine("sitemapindex")
        BggClient.engine = { engine }

        val response = BggClient.sitemapIndex().call()

        val request = engine.requestHistory[0]
        assertThat(engine.requestHistory).hasSize(1)
        assertThat(request.method).isEqualTo(HttpMethod.Get)
        assertThat(request.headers)
            .isEqualTo(
                TestUtils.DEFAULT_HEADERS
            )
        assertThat(request.url).isEqualTo(Url("https://boardgamegeek.com/sitemapindex"))
        assertThat(response.isError()).isFalse()
        assertThat(response.isSuccess()).isTrue()
        assertThat(response.data?.sitemaps).hasSize(49)
        val sitemaps =
            buildMap<SitemapLocationType, MutableList<SitemapLocation>> {
                response.data!!.sitemaps.forEach {
                    compute(it.type) { _, value -> (value ?: mutableListOf()).apply { add(it) } }
                }
            }
        assertThat(sitemaps.keys).hasSize(29)
        assertThat(sitemaps.containsKey(SitemapLocationType.UNKNOWN)).isFalse()
        assertThat(sitemaps[SitemapLocationType.BOARD_GAMES]).hasSize(2)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_ACCESSORIES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_ACCESSORY_FAMILIES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_ACCESSORY_VERSIONS]).hasSize(2)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_ARTISTS]).hasSize(3)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_AUTHORS]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_COMPILATIONS]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_DESIGNERS]).hasSize(4)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_EVENTS]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_EXPANSIONS]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_FAMILIES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_IMPLEMENTATIONS]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_ISSUES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_ISSUE_ARTICLES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_ISSUE_VERSIONS]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_PERIODICALS]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_PUBLISHERS]).hasSize(3)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_SLEEVES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_SLEEVE_MANUFACTURERS]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_SUB_DOMAINS]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.BOARD_GAME_VERSIONS]).hasSize(3)
        assertThat(sitemaps[SitemapLocationType.CARD_SETS]).hasSize(2)
        assertThat(sitemaps[SitemapLocationType.CARD_TYPES]).hasSize(3)
        assertThat(sitemaps[SitemapLocationType.FILES]).hasSize(2)
        assertThat(sitemaps[SitemapLocationType.GEEK_LISTS]).hasSize(2)
        assertThat(sitemaps[SitemapLocationType.IMAGES]).hasSize(2)
        assertThat(sitemaps[SitemapLocationType.THREADS]).hasSize(4)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_BOARD_GAMES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.WIKI_PAGES]).hasSize(1)
    }

    @Test
    fun `Makes a request to retrieve the sitemap index - videogamegeek_com`() = runBlocking {
        val engine = TestUtils.setupMockEngine("sitemapindex?domain=videogamegeek")
        BggClient.engine = { engine }

        val response = BggClient.sitemapIndex(Domain.VIDEO_GAME_GEEK).call()

        val request = engine.requestHistory[0]
        assertThat(engine.requestHistory).hasSize(1)
        assertThat(request.method).isEqualTo(HttpMethod.Get)
        assertThat(request.headers)
            .isEqualTo(
                TestUtils.DEFAULT_HEADERS
            )
        assertThat(request.url).isEqualTo(Url("https://videogamegeek.com/sitemapindex"))
        assertThat(response.isError()).isFalse()
        assertThat(response.isSuccess()).isTrue()
        assertThat(response.data?.sitemaps).hasSize(71)
        val sitemaps =
            buildMap<SitemapLocationType, MutableList<SitemapLocation>> {
                response.data!!.sitemaps.forEach {
                    compute(it.type) { _, value -> (value ?: mutableListOf()).apply { add(it) } }
                }
            }
        assertThat(sitemaps.keys).hasSize(21)
        assertThat(sitemaps.containsKey(SitemapLocationType.UNKNOWN)).isFalse()
        assertThat(sitemaps[SitemapLocationType.FILES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.GEEK_LISTS]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.IMAGES]).hasSize(23)
        assertThat(sitemaps[SitemapLocationType.THREADS]).hasSize(4)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAMES]).hasSize(6)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_BOARD_GAMES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_CHARACTERS]).hasSize(3)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_CHARACTER_VERSIONS]).hasSize(5)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_COMPILATION]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_DEVELOPER]).hasSize(2)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_EXPANSION]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_FRANCHISE]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_GENRES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_HARDWARE]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_HARDWARE_VERSION]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_PLATFORM]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_PUBLISHER]).hasSize(2)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_SERIES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_THEMES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.VIDEO_GAME_VERSION]).hasSize(13)
        assertThat(sitemaps[SitemapLocationType.WIKI_PAGES]).hasSize(1)
    }

    @Test
    fun `Makes a request to retrieve the sitemap index - rpggeek_com`() = runBlocking {
        val engine = TestUtils.setupMockEngine("sitemapindex?domain=rpggeek")
        BggClient.engine = { engine }

        val response = BggClient.sitemapIndex(Domain.RPG_GEEK).call()

        val request = engine.requestHistory[0]
        assertThat(engine.requestHistory).hasSize(1)
        assertThat(request.method).isEqualTo(HttpMethod.Get)
        assertThat(request.headers)
            .isEqualTo(
                TestUtils.DEFAULT_HEADERS
            )
        assertThat(request.url).isEqualTo(Url("https://rpggeek.com/sitemapindex"))
        assertThat(response.isError()).isFalse()
        assertThat(response.isSuccess()).isTrue()
        assertThat(response.data?.sitemaps).hasSize(131)
        val sitemaps =
            buildMap<SitemapLocationType, MutableList<SitemapLocation>> {
                response.data!!.sitemaps.forEach {
                    compute(it.type) { _, value -> (value ?: mutableListOf()).apply { add(it) } }
                }
            }
        assertThat(sitemaps.keys).hasSize(23)
        assertThat(sitemaps.containsKey(SitemapLocationType.UNKNOWN)).isFalse()
        assertThat(sitemaps[SitemapLocationType.FILES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.GEEK_LISTS]).hasSize(2)
        assertThat(sitemaps[SitemapLocationType.IMAGES]).hasSize(41)
        assertThat(sitemaps[SitemapLocationType.RPG]).hasSize(2)
        assertThat(sitemaps[SitemapLocationType.RPG_ARTISTS]).hasSize(5)
        assertThat(sitemaps[SitemapLocationType.RPG_CATEGORIES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.RPG_DESIGNERS]).hasSize(6)
        assertThat(sitemaps[SitemapLocationType.RPG_FAMILIES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.RPG_GENRES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.RPG_ISSUE]).hasSize(2)
        assertThat(sitemaps[SitemapLocationType.RPG_ISSUE_ARTICLE]).hasSize(14)
        assertThat(sitemaps[SitemapLocationType.RPG_ISSUE_VERSION]).hasSize(3)
        assertThat(sitemaps[SitemapLocationType.RPG_ITEM]).hasSize(15)
        assertThat(sitemaps[SitemapLocationType.RPG_ITEM_VERSION]).hasSize(20)
        assertThat(sitemaps[SitemapLocationType.RPG_MECHANIC]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.RPG_PERIODICAL]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.RPG_PRODUCERS]).hasSize(3)
        assertThat(sitemaps[SitemapLocationType.RPG_PUBLISHER]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.RPG_SERIES]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.RPG_SETTING]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.RPG_SYSTEM]).hasSize(1)
        assertThat(sitemaps[SitemapLocationType.THREADS]).hasSize(7)
        assertThat(sitemaps[SitemapLocationType.WIKI_PAGES]).hasSize(1)
    }

    @Nested
    inner class Diffuse {
        @Test
        fun `Automatically requests all pages in the sitemap index`() = runBlocking {
            val engine =
                MockEngine(MockEngineConfig().apply { addHandler { setupSitemapResponses(it) } })
            BggClient.engine = { engine }

            val response = BggClient.sitemapIndex().diffuse().call()

            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/sitemapindex"),
                    Url("https://boardgamegeek.com/sitemap_geekitems_boardgame_page_1"),
                    Url("https://boardgamegeek.com/sitemap_geekitems_boardgameversion_page_1"),
                    Url("https://boardgamegeek.com/sitemap_files_page_1"),
                )
            assertThat(response.isError()).isFalse()
            assertThat(response.isSuccess()).isTrue()
            assertThat(response.data).hasSize(3)
            assertThat(response.data!![SitemapLocationType.BOARD_GAMES]).hasSize(10)
            assertThat(response.data!![SitemapLocationType.BOARD_GAME_VERSIONS]).hasSize(9)
            assertThat(response.data!![SitemapLocationType.FILES]).hasSize(11)
        }

        @Test
        fun `Skips empty responses`() = runBlocking {
            val engine =
                TestUtils.setupMockEngine("sitemapindex.diffuse", "thread", "thread", "thread")
            BggClient.engine = { engine }

            val response = BggClient.sitemapIndex().diffuse().call()

            assertThat(engine.requestHistory.map { it.url })
                .containsExactly(
                    Url("https://boardgamegeek.com/sitemapindex"),
                    Url("https://boardgamegeek.com/sitemap_geekitems_boardgame_page_1"),
                    Url("https://boardgamegeek.com/sitemap_geekitems_boardgameversion_page_1"),
                    Url("https://boardgamegeek.com/sitemap_files_page_1"),
                )
            assertThat(response.isError()).isFalse()
            assertThat(response.isSuccess()).isTrue()
            assertThat(response.data).isEmpty()
        }

        private fun MockRequestHandleScope.setupSitemapResponses(requestData: HttpRequestData) =
            if (requestData.url.toString().endsWith("sitemapindex")) {
                respondOk(String(xml("sitemapindex.diffuse").readAllBytes()))
            } else if (requestData.url.toString().endsWith("boardgame_page_1")) {
                respondOk(String(xml("sitemap_boardgame_page1").readAllBytes()))
            } else if (requestData.url.toString().endsWith("boardgameversion_page_1")) {
                respondOk(String(xml("sitemap_boardgameversion_page1").readAllBytes()))
            } else if (requestData.url.toString().endsWith("files_page_1")) {
                respondOk(String(xml("sitemap_files_page1").readAllBytes()))
            } else {
                respondBadRequest()
            }
    }
}
