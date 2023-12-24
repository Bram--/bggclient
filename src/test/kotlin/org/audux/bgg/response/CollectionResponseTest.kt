/**
 * Copyright 2023 Bram Wijnands
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
import com.google.common.truth.Truth
import java.time.LocalDateTime
import org.audux.bgg.common.Rank
import org.audux.bgg.common.Ratings
import org.audux.bgg.common.ThingType
import org.audux.bgg.module.BggXmlObjectMapper
import org.audux.bgg.module.appModule
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

class CollectionResponseTest : KoinTest {
    @JvmField
    @RegisterExtension
    @Suppress("unused")
    val koinTestExtension = KoinTestExtension.create { modules(appModule) }

    private val mapper: ObjectMapper by inject(named<BggXmlObjectMapper>())

    @Test
    fun `Parses empty response`() {
        val results =
            mapper.readValue(TestUtils.xml("collection?username=empty"), Collection::class.java)

        Truth.assertThat(results.items).hasSize(0)
    }

    @Test
    fun `Parses collection items`() {
        val results =
            mapper.readValue(
                TestUtils.xml(
                    "collection?username=novaeux&stats=1&subtype=boardgame&excludesubtype=boardgameexpansion"
                ),
                Collection::class.java
            )

        Truth.assertThat(results.items).hasSize(105)
        Truth.assertThat(results.items[0])
            .isEqualTo(
                CollectionItem(
                    collectionId = 90725673,
                    objectId = 68448,
                    type = ThingType.BOARD_GAME,
                    name = "7 Wonders",
                    yearPublished = 2010,
                    image =
                        "https://cf.geekdo-images.com/35h9Za_JvMMMtx_92kT0Jg__original/img/jt70jJDZ1y1FWJs4ZQf5FI8APVY=/0x0/filters:format(jpeg)/pic7149798.jpg",
                    thumbnail =
                        "https://cf.geekdo-images.com/35h9Za_JvMMMtx_92kT0Jg__thumb/img/BUOso8b0M1aUOkU80FWlhE8uuxc=/fit-in/200x150/filters:strip_icc()/pic7149798.jpg",
                    numPlays = 1,
                    status =
                        Status(
                            previouslyOwned = true,
                            lastModified = LocalDateTime.of(2023, 3, 20, 4, 58, 43),
                        ),
                    stats =
                        CollectionStatistics(
                            minimumPlayers = 2,
                            maximumPlayers = 7,
                            minimumPlayTime = 30,
                            maximumPlayTime = 30,
                            playingTime = 30,
                            numOwned = 137568,
                            ratings =
                                Ratings(
                                    value = "7",
                                    usersRated = WrappedValue(101690),
                                    average = WrappedValue(7.69089),
                                    bayesAverage = WrappedValue(7.58314),
                                    stdDev = WrappedValue(1.277),
                                    median = WrappedValue(0),
                                    ranks =
                                        listOf(
                                            Rank(
                                                id = 1,
                                                name = "boardgame",
                                                friendlyName = "Board Game Rank",
                                                type = "subtype",
                                                value = "89",
                                                bayesAverage = "7.58314"
                                            ),
                                            Rank(
                                                id = 5497,
                                                name = "strategygames",
                                                friendlyName = "Strategy Game Rank",
                                                type = "family",
                                                value = "90",
                                                bayesAverage = "7.5477"
                                            ),
                                            Rank(
                                                id = 5499,
                                                name = "familygames",
                                                friendlyName = "Family Game Rank",
                                                type = "family",
                                                value = "12",
                                                bayesAverage = "7.57952"
                                            ),
                                        ),
                                ),
                        )
                )
            )
    }

    @Test
    fun `Parses rpg collection items`() {
        val results =
            mapper.readValue(
                TestUtils.xml("collection?username=novaeux&stats=1&subtype=rpgitem"),
                Collection::class.java
            )

        Truth.assertThat(results.items).hasSize(1)
        Truth.assertThat(results.items[0].name).isEqualTo("Alice is Missing")
    }
}
