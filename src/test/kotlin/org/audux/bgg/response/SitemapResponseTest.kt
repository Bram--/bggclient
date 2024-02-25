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
package org.audux.bgg.response

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.truth.Truth.assertThat
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test

/** Unit test for [Hot] data classes. */
class SitemapResponseTest {
    private val mapper: ObjectMapper = TestUtils.getBggClientMapper()

    @Test
    fun `Parses a Sitemap`() {
        val results =
            mapper.readValue(TestUtils.xml("sitemap_boardgame_page1"), Sitemap::class.java)

        assertThat(results.sitemaps).hasSize(10)
        assertThat(results.sitemaps[0])
            .isEqualTo(
                defaultSitemapUrl(location = "https://boardgamegeek.com/boardgame/1/die-macher")
            )
        assertThat(results.sitemaps[1])
            .isEqualTo(
                defaultSitemapUrl(location = "https://boardgamegeek.com/boardgame/2/dragonmaster")
            )
        assertThat(results.sitemaps[2])
            .isEqualTo(
                defaultSitemapUrl(location = "https://boardgamegeek.com/boardgame/3/samurai")
            )
        assertThat(results.sitemaps[3])
            .isEqualTo(
                defaultSitemapUrl(location = "https://boardgamegeek.com/boardgame/4/tal-der-konige")
            )
        assertThat(results.sitemaps[4])
            .isEqualTo(
                defaultSitemapUrl(location = "https://boardgamegeek.com/boardgame/5/acquire")
            )
        assertThat(results.sitemaps[5])
            .isEqualTo(
                defaultSitemapUrl(
                    location = "https://boardgamegeek.com/boardgame/6/mare-mediterraneum"
                )
            )
        assertThat(results.sitemaps[6])
            .isEqualTo(
                defaultSitemapUrl(location = "https://boardgamegeek.com/boardgame/7/cathedral")
            )
        assertThat(results.sitemaps[7])
            .isEqualTo(
                defaultSitemapUrl(location = "https://boardgamegeek.com/boardgame/8/lords-creation")
            )
        assertThat(results.sitemaps[8])
            .isEqualTo(
                defaultSitemapUrl(location = "https://boardgamegeek.com/boardgame/9/el-caballero")
            )
        assertThat(results.sitemaps[9])
            .isEqualTo(
                defaultSitemapUrl(location = "https://boardgamegeek.com/boardgame/10/elfenland")
            )
    }

    private fun defaultSitemapUrl(location: String) =
        SitemapUrl(
            location = location,
            changeFrequency = "daily",
            priority = 1.0,
            lastModified = null
        )
}
