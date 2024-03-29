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
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test

/** Unit test for [Hot] data classes. */
class SitemapIndexResponseTest {
    private val mapper: ObjectMapper = TestUtils.getBggClientMapper()

    @Test
    fun `is (K)Serializable`() {
        val sitemapIndex = mapper.readValue(TestUtils.xml("sitemapindex"), SitemapIndex::class.java)
        val encodedSitemapIndex = Json.encodeToString(sitemapIndex)

        assertThat(Json.decodeFromString<SitemapIndex>(encodedSitemapIndex)).isEqualTo(sitemapIndex)
    }

    @Test
    fun `Parses a Sitemap Index`() {
        val results = mapper.readValue(TestUtils.xml("sitemapindex"), SitemapIndex::class.java)

        assertThat(results.sitemaps).hasSize(49)
        assertThat(results.sitemaps[0])
            .isEqualTo(
                SitemapLocation(
                    location = "https://boardgamegeek.com/sitemap_geekitems_boardgame_page_1"
                )
            )
        assertThat(results.sitemaps[48])
            .isEqualTo(SitemapLocation(location = "https://boardgamegeek.com/sitemap_files_page_2"))
    }
}
