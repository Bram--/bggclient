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
import org.audux.bgg.common.FamilyType
import org.audux.bgg.common.Link
import org.audux.bgg.common.Name
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test

/** Unit test for [Family] data classes. */
class FamilyResponseTest {
    private val mapper: ObjectMapper = TestUtils.getBggClientMapper()

    @Test
    fun `Parses empty response`() {
        val results = mapper.readValue(TestUtils.xml("family?id=-1"), Family::class.java)

        assertThat(results.items).hasSize(0)
    }

    @Test
    fun `is (K)Serializable`() {
        val family = mapper.readValue(TestUtils.xml("family"), Family::class.java)
        val encodedFamily = Json.encodeToString(family)

        assertThat(Json.decodeFromString<Family>(encodedFamily)).isEqualTo(family)
    }

    @Test
    fun `Parses a family with it's associated links`() {
        val results = mapper.readValue(TestUtils.xml("family"), Family::class.java)

        assertThat(results.items).hasSize(1)
        assertThat(results.items[0])
            .isEqualTo(
                FamilyItem(
                    id = 50152,
                    type = FamilyType.BOARD_GAME_FAMILY,
                    name = Name("History: Industrial Revolution", type = "primary", sortIndex = 1),
                    description =
                        "Games (expansions, promos, etc.) featuring the Industrial Revolution in theme or gameplay.&#10;&#10;&#10;The Industrial Revolution period (end of 18th and beginning of 19th centuries) and the development of the industries.&#10;&#10;",
                    links =
                        listOf(
                            Link(
                                type = "boardgamefamily",
                                id = 65901,
                                value = "Age of Industry",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 99424,
                                value = "Age of Industry Expansion #1: Japan and Minnesota",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 136217,
                                value = "Age of Industry Expansion: Belgium & USSR",
                                inbound = true
                            ),
                        )
                )
            )
    }
}
