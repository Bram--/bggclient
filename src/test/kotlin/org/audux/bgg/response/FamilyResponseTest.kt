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
import org.audux.bgg.common.FamilyType
import org.audux.bgg.common.Link
import org.audux.bgg.common.Name
import org.audux.bgg.module.BggXmlObjectMapper
import org.audux.bgg.module.appModule
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

class FamilyResponseTest : KoinTest {
    @JvmField
    @RegisterExtension
    @Suppress("unused")
    val koinTestExtension = KoinTestExtension.create { modules(appModule) }

    private val mapper: ObjectMapper by inject(named<BggXmlObjectMapper>())

    @Test
    fun `Parses empty response`() {
        val results = mapper.readValue(TestUtils.xml("family?id=-1"), Family::class.java)

        assertThat(results.items).hasSize(0)
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
                            Link(
                                type = "boardgamefamily",
                                id = 121285,
                                value = "Age of Industry Expansion: Great Lakes & South Africa",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 366719,
                                value = "American Mogul",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 377613,
                                value = "Anno 1800: Der Solo-Modus",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 406856,
                                value = "Anno 1800: Die Erweiterung",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 311193,
                                value = "Anno 1800: The Board Game",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 154825,
                                value = "Arkwright",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 300664,
                                value = "Arkwright: The Card Game",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 359130,
                                value = "Arkwright: The Card Game – Game Brewer Promo Pack",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 224517,
                                value = "Brass: Birmingham",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 28720,
                                value = "Brass: Lancashire",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 249277,
                                value = "Brazil: Imperial",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 365738,
                                value = "Brazil: Imperial – Autômato",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 19995,
                                value = "Canal Mania",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 131001,
                                value = "City of the Century",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 349961,
                                value = "Cotton Trade: 1800-1860",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 170951,
                                value = "The Foreign King",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 154809,
                                value = "Nippon",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 396790,
                                value = "Nucleum",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 408223,
                                value = "Nucleum: Australia",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 188472,
                                value = "Portugal (fan expansion for Age of Industry)",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 204,
                                value = "Stephenson's Rocket",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 343927,
                                value = "Union Stockyards",
                                inbound = true
                            ),
                            Link(
                                type = "boardgamefamily",
                                id = 376142,
                                value = "Victoria: The Board Game",
                                inbound = true
                            )
                        )
                )
            )
    }
}
