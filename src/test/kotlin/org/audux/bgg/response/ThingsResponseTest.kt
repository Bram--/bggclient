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
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.audux.bgg.common.ThingType
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/** Test class for [Things] data classes. */
class ThingsResponseTest {
    private val mapper: ObjectMapper = TestUtils.getBggClientMapper()

    @Test
    fun `parses an empty response`() {
        val things = mapper.readValue(TestUtils.xml("thing?id=-1"), Things::class.java)

        assertThat(things.things).hasSize(0)
    }

    @Test
    fun `parses an item wih an error field`() {
        val things = mapper.readValue(TestUtils.xml("thing?id=with-error"), Things::class.java)

        assertThat(things.things).hasSize(1)
        assertThat(things.things[0].error).isEqualTo("Exception")
    }

    @Test
    fun `parses multiple things`() {
        val things = mapper.readValue(TestUtils.xml("thing?id=1,2,3"), Things::class.java)

        assertThat(things.things).hasSize(3)
    }

    @Test
    fun `is (K)Serializable`() {
        val things =
            mapper.readValue(
                TestUtils.xml(
                    "thing?id=396790&stats=1&ratingcomments=1&versions=1&marketplace=1&videos=1"
                ),
                Things::class.java,
            )
        val thing = things.things[0]
        val encodedThing = Json.encodeToString(thing)

        assertThat(Json.decodeFromString<Thing>(encodedThing)).isEqualTo(thing)
    }

    @Nested
    inner class BoardGame {
        @Test
        fun `parses a simple response`() {
            val things = mapper.readValue(TestUtils.xml("thing?id=1"), Things::class.java)

            assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            assertThat(thing.name).isEqualTo("Die Macher")
            assertThat(thing.type).isEqualTo(ThingType.BOARD_GAME)
            assertThat(thing.names).hasSize(3)
            assertThat(thing.description).hasLength(1268)
            assertThat(thing.yearPublished).isEqualTo(1986)
            assertThat(thing.minPlayers).isEqualTo(3)
            assertThat(thing.maxPlayers).isEqualTo(5.0)
            assertThat(thing.playingTimeInMinutes).isEqualTo(240)
            assertThat(thing.minPlayingTimeInMinutes).isEqualTo(240)
            assertThat(thing.maxPlayingTimeInMinutes).isEqualTo(240)
            assertThat(thing.minAge).isEqualTo(14)
        }

        @Test
        fun `parses polls`() {
            val things = mapper.readValue(TestUtils.xml("thing?id=1"), Things::class.java)

            assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            assertThat(thing.polls).hasSize(3)
            assertThat(thing.polls.map { it.javaClass })
                .containsExactly(
                    PlayerAgePoll::class.java,
                    LanguageDependencePoll::class.java,
                    NumberOfPlayersPoll::class.java,
                )
        }

        @Test
        fun `parses poll-summaries`() {
            val things = mapper.readValue(TestUtils.xml("thing?id=1"), Things::class.java)

            assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            assertThat(thing.pollSummary).hasSize(1)
            assertThat(thing.pollSummary[0])
                .isEqualTo(
                    PollSummary(
                        name = "suggested_numplayers",
                        title = "User Suggested Number of Players",
                        results =
                            listOf(
                                PollSummaryResult(
                                    name = "bestwith",
                                    value = "Best with 3–4 players",
                                ),
                                PollSummaryResult(
                                    name = "recommmendedwith",
                                    value = "Recommended with 2–5 players",
                                ),
                            ),
                    )
                )
        }

        @Test
        fun `parses poll - PlayerAgePoll`() {
            val things = mapper.readValue(TestUtils.xml("thing?id=1"), Things::class.java)

            val thing = things.things[0]
            val playerAgePoll = thing.polls.findLast { it is PlayerAgePoll }!! as PlayerAgePoll
            assertThat(playerAgePoll.title).isEqualTo("User Suggested Player Age")
            assertThat(playerAgePoll.totalVotes).isEqualTo(31)
            val groupedVotes = playerAgePoll.results
            assertThat(groupedVotes).hasSize(12)
            assertThat(groupedVotes[0].value).isEqualTo("2")
            assertThat(groupedVotes[0].numberOfVotes).isEqualTo(0)
            assertThat(groupedVotes[1].value).isEqualTo("3")
            assertThat(groupedVotes[1].numberOfVotes).isEqualTo(0)
            assertThat(groupedVotes[2].value).isEqualTo("4")
            assertThat(groupedVotes[2].numberOfVotes).isEqualTo(0)
            assertThat(groupedVotes[3].value).isEqualTo("5")
            assertThat(groupedVotes[3].numberOfVotes).isEqualTo(0)
            assertThat(groupedVotes[4].value).isEqualTo("6")
            assertThat(groupedVotes[4].numberOfVotes).isEqualTo(0)
            assertThat(groupedVotes[5].value).isEqualTo("8")
            assertThat(groupedVotes[5].numberOfVotes).isEqualTo(0)
            assertThat(groupedVotes[6].value).isEqualTo("10")
            assertThat(groupedVotes[6].numberOfVotes).isEqualTo(0)
            assertThat(groupedVotes[7].value).isEqualTo("12")
            assertThat(groupedVotes[7].numberOfVotes).isEqualTo(6)
            assertThat(groupedVotes[8].value).isEqualTo("14")
            assertThat(groupedVotes[8].numberOfVotes).isEqualTo(18)
            assertThat(groupedVotes[9].value).isEqualTo("16")
            assertThat(groupedVotes[9].numberOfVotes).isEqualTo(4)
            assertThat(groupedVotes[10].value).isEqualTo("18")
            assertThat(groupedVotes[10].numberOfVotes).isEqualTo(2)
            assertThat(groupedVotes[11].value).isEqualTo("21 and up")
            assertThat(groupedVotes[11].numberOfVotes).isEqualTo(1)
        }

        @Test
        fun `parses poll - LanguageDependencePoll`() {
            val things = mapper.readValue(TestUtils.xml("thing?id=1"), Things::class.java)

            val thing = things.things[0]
            val languageDependencePoll =
                thing.polls.findLast { it is LanguageDependencePoll }!! as LanguageDependencePoll
            assertThat(languageDependencePoll.title).isEqualTo("Language Dependence")
            assertThat(languageDependencePoll.totalVotes).isEqualTo(48)
            val groupedVotes = languageDependencePoll.results
            assertThat(groupedVotes[0].level).isEqualTo(1)
            assertThat(groupedVotes[0].numberOfVotes).isEqualTo(36)
            assertThat(groupedVotes[0].value).isEqualTo("No necessary in-game text")
            assertThat(groupedVotes[1].level).isEqualTo(2)
            assertThat(groupedVotes[1].numberOfVotes).isEqualTo(5)
            assertThat(groupedVotes[1].value)
                .isEqualTo("Some necessary text - easily memorized or small crib sheet")
            assertThat(groupedVotes[2].level).isEqualTo(3)
            assertThat(groupedVotes[2].numberOfVotes).isEqualTo(7)
            assertThat(groupedVotes[2].value)
                .isEqualTo("Moderate in-game text - needs crib sheet or paste ups")
            assertThat(groupedVotes[3].level).isEqualTo(4)
            assertThat(groupedVotes[3].numberOfVotes).isEqualTo(0)
            assertThat(groupedVotes[3].value)
                .isEqualTo("Extensive use of text - massive conversion needed to be playable")
            assertThat(groupedVotes[4].level).isEqualTo(5)
            assertThat(groupedVotes[4].numberOfVotes).isEqualTo(0)
            assertThat(groupedVotes[4].value).isEqualTo("Unplayable in another language")
        }

        @Test
        fun `parses poll - NumberOfPlayersPoll`() {
            val things = mapper.readValue(TestUtils.xml("thing?id=1"), Things::class.java)

            val thing = things.things[0]
            val numberOfPlayersPoll =
                thing.polls.findLast { it is NumberOfPlayersPoll }!! as NumberOfPlayersPoll
            assertThat(numberOfPlayersPoll.title).isEqualTo("User Suggested Number of Players")
            assertThat(numberOfPlayersPoll.totalVotes).isEqualTo(136)

            val groupedVotes = numberOfPlayersPoll.results
            assertThat(groupedVotes[0].numberOfPlayers).isEqualTo("1")
            assertThat(groupedVotes[0].results[0].value).isEqualTo("Best")
            assertThat(groupedVotes[0].results[0].numberOfVotes).isEqualTo(0)
            assertThat(groupedVotes[0].results[1].value).isEqualTo("Recommended")
            assertThat(groupedVotes[0].results[1].numberOfVotes).isEqualTo(1)
            assertThat(groupedVotes[0].results[2].value).isEqualTo("Not Recommended")
            assertThat(groupedVotes[0].results[2].numberOfVotes).isEqualTo(86)

            assertThat(groupedVotes[1].numberOfPlayers).isEqualTo("2")
            assertThat(groupedVotes[1].results[0].value).isEqualTo("Best")
            assertThat(groupedVotes[1].results[0].numberOfVotes).isEqualTo(0)
            assertThat(groupedVotes[1].results[1].value).isEqualTo("Recommended")
            assertThat(groupedVotes[1].results[1].numberOfVotes).isEqualTo(1)
            assertThat(groupedVotes[1].results[2].value).isEqualTo("Not Recommended")
            assertThat(groupedVotes[1].results[2].numberOfVotes).isEqualTo(88)

            assertThat(groupedVotes[2].numberOfPlayers).isEqualTo("3")
            assertThat(groupedVotes[2].results[0].value).isEqualTo("Best")
            assertThat(groupedVotes[2].results[0].numberOfVotes).isEqualTo(2)
            assertThat(groupedVotes[2].results[1].value).isEqualTo("Recommended")
            assertThat(groupedVotes[2].results[1].numberOfVotes).isEqualTo(26)
            assertThat(groupedVotes[2].results[2].value).isEqualTo("Not Recommended")
            assertThat(groupedVotes[2].results[2].numberOfVotes).isEqualTo(76)

            assertThat(groupedVotes[3].numberOfPlayers).isEqualTo("4")
            assertThat(groupedVotes[3].results[0].value).isEqualTo("Best")
            assertThat(groupedVotes[3].results[0].numberOfVotes).isEqualTo(25)
            assertThat(groupedVotes[3].results[1].value).isEqualTo("Recommended")
            assertThat(groupedVotes[3].results[1].numberOfVotes).isEqualTo(86)
            assertThat(groupedVotes[3].results[2].value).isEqualTo("Not Recommended")
            assertThat(groupedVotes[3].results[2].numberOfVotes).isEqualTo(9)

            assertThat(groupedVotes[4].numberOfPlayers).isEqualTo("5")
            assertThat(groupedVotes[4].results[0].value).isEqualTo("Best")
            assertThat(groupedVotes[4].results[0].numberOfVotes).isEqualTo(116)
            assertThat(groupedVotes[4].results[1].value).isEqualTo("Recommended")
            assertThat(groupedVotes[4].results[1].numberOfVotes).isEqualTo(11)
            assertThat(groupedVotes[4].results[2].value).isEqualTo("Not Recommended")
            assertThat(groupedVotes[4].results[2].numberOfVotes).isEqualTo(2)

            assertThat(groupedVotes[5].numberOfPlayers).isEqualTo("5+")
            assertThat(groupedVotes[5].results[0].value).isEqualTo("Best")
            assertThat(groupedVotes[5].results[0].numberOfVotes).isEqualTo(1)
            assertThat(groupedVotes[5].results[1].value).isEqualTo("Recommended")
            assertThat(groupedVotes[5].results[1].numberOfVotes).isEqualTo(0)
            assertThat(groupedVotes[5].results[2].value).isEqualTo("Not Recommended")
            assertThat(groupedVotes[5].results[2].numberOfVotes).isEqualTo(62)
        }

        @Test
        fun `parses ratingcomments`() {
            val things =
                mapper.readValue(
                    TestUtils.xml(
                        "thing?id=396790&stats=1&ratingcomments=1&versions=1&marketplace=1&videos=1"
                    ),
                    Things::class.java,
                )

            assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            assertThat(thing.comments?.page).isEqualTo(1)
            assertThat(thing.comments?.totalItems).isEqualTo(1402)
            assertThat(thing.comments?.comments).hasSize(100)
            assertThat(thing.comments?.comments?.get(0)?.rating).isEqualTo("10")
            assertThat(thing.comments?.comments?.get(0)?.value).contains("This game is amazing")
            assertThat(thing.comments?.comments?.get(0)?.username).contains("actiondan87")

            val ratings = thing.comments?.comments?.map { it.rating }
            assertThat(ratings).hasSize(100)
            assertThat(ratings).containsExactlyElementsIn(Array(100) { "10" })
        }

        @Test
        fun `parses links`() {
            val things =
                mapper.readValue(
                    TestUtils.xml(
                        "thing?id=396790&stats=1&ratingcomments=1&versions=1&marketplace=1&videos=1"
                    ),
                    Things::class.java,
                )

            assertThat(things.things).hasSize(1)
            val links = things.things[0].links
            assertThat(links).hasSize(33)
            assertThat(links[0].id).isEqualTo(1021)
            assertThat(links[0].inbound).isNull()
            assertThat(links[0].value).isEqualTo("Economic")
            assertThat(links[0].type).isEqualTo("boardgamecategory")
            assertThat(links.filter { it.type == "boardgamecategory" }).hasSize(3)
            assertThat(links.filter { it.type == "boardgamemechanic" }).hasSize(10)
            assertThat(links.filter { it.type == "boardgamefamily" }).hasSize(5)
            assertThat(links.filter { it.type == "boardgameexpansion" }).hasSize(2)
            assertThat(links.filter { it.type == "boardgamedesigner" }).hasSize(2)
            assertThat(links.filter { it.type == "boardgameartist" }).hasSize(3)
            assertThat(links.filter { it.type == "boardgamepublisher" }).hasSize(8)
        }

        @Test
        fun `parses versions`() {
            val things =
                mapper.readValue(
                    TestUtils.xml(
                        "thing?id=396790&stats=1&ratingcomments=1&versions=1&marketplace=1&videos=1"
                    ),
                    Things::class.java,
                )

            assertThat(things.things).hasSize(1)
            val versions = things.things[0].versions
            assertThat(versions).hasSize(8)
            assertThat(versions[0].id).isEqualTo(685632)
            assertThat(versions[0].type).isEqualTo("boardgameversion")
            assertThat(versions[0].yearPublished).isEqualTo(2024)
            assertThat(versions[0].productCode).isEqualTo("8593085104517")
            assertThat(versions[0].width).isEqualTo(11.7)
            assertThat(versions[0].length).isEqualTo(11.7)
            assertThat(versions[0].depth).isEqualTo(2.8)
            assertThat(versions[0].weight).isEqualTo(0)
            assertThat(versions[0].thumbnail)
                .isEqualTo(
                    "https://cf.geekdo-images.com/D3TEgieEudyIiY70hkQiKw__thumb/img/DhEkGYrwkfDTAUWK7EFjwiVdBMU=/fit-in/200x150/filters:strip_icc()/pic7800352.png"
                )
            assertThat(versions[0].image)
                .isEqualTo(
                    "https://cf.geekdo-images.com/D3TEgieEudyIiY70hkQiKw__original/img/G9XIuskUuVU0he4ywThrz9bbpEA=/0x0/filters:format(png)/pic7800352.png"
                )
            assertThat(versions[0].names).hasSize(1)
            assertThat(versions[0].name).isEqualTo("Czech edition")

            val links = versions[0].links
            assertThat(links).hasSize(7)
        }

        @Test
        fun `parses statistics`() {
            val things =
                mapper.readValue(
                    TestUtils.xml(
                        "thing?id=396790&stats=1&ratingcomments=1&versions=1&marketplace=1&videos=1"
                    ),
                    Things::class.java,
                )

            assertThat(things.things).hasSize(1)
            val stats = things.things[0].statistics!!
            val ratings = stats.ratings
            assertThat(stats.page).isEqualTo(1)
            assertThat(ratings.usersRated).isEqualTo(1373)
            assertThat(ratings.average).isEqualTo(8.27146)
            assertThat(ratings.bayesAverage).isEqualTo(6.57535)
            assertThat(ratings.ranks).hasSize(2)
            assertThat(ratings.ranks[0].type).isEqualTo("subtype")
            assertThat(ratings.ranks[0].id).isEqualTo(1)
            assertThat(ratings.ranks[0].name).isEqualTo("boardgame")
            assertThat(ratings.ranks[0].friendlyName).isEqualTo("Board Game Rank")
            assertThat(ratings.ranks[0].value).isEqualTo("1045")
            assertThat(ratings.ranks[0].bayesAverage).isEqualTo("6.57535")
            assertThat(ratings.stdDev).isEqualTo(1.25647)
            assertThat(ratings.median).isAtLeast(0)
            assertThat(ratings.owned).isEqualTo(2911)
            assertThat(ratings.trading).isEqualTo(9)
            assertThat(ratings.wanting).isEqualTo(381)
            assertThat(ratings.wishing).isEqualTo(3076)
            assertThat(ratings.numComments).isEqualTo(324)
            assertThat(ratings.numWeights).isEqualTo(173)
            assertThat(ratings.averageWeight).isEqualTo(4.0578)
        }

        @Test
        fun `parses marketplace listings`() {
            val things =
                mapper.readValue(
                    TestUtils.xml(
                        "thing?id=396790&stats=1&ratingcomments=1&versions=1&marketplace=1&videos=1"
                    ),
                    Things::class.java,
                )

            assertThat(things.things).hasSize(1)
            val listings = things.things[0].listings
            assertThat(listings).hasSize(11)
            assertThat(listings[0].listDate).isEqualTo(LocalDateTime.of(2023, 10, 6, 19, 41, 25))
            assertThat(listings[0].listDate).isEqualTo(LocalDateTime.of(2023, 10, 6, 19, 41, 25))
            assertThat(listings[0].price.value).isEqualTo(80.00)
            assertThat(listings[0].price.currency).isEqualTo("USD")
            assertThat(listings[0].condition).isEqualTo("new")
            assertThat(listings[0].notes).startsWith("Game is NIS")
            assertThat(listings[0].webLink.title).isEqualTo("marketlisting")
            assertThat(listings[0].webLink.href)
                .isEqualTo(URI.create("https://boardgamegeek.com/market/product/3276360"))
        }
    }

    @Nested
    inner class BoardGameExpansion {
        @Test
        fun `Parses all available data`() {
            val things =
                mapper.readValue(
                    TestUtils.xml(
                        "thing?id=307683&stats=1&ratingcomments=1&versions=1&marketplace=1&videos=1"
                    ),
                    Things::class.java,
                )

            assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            assertThat(thing.name).isEqualTo("Final Girl: The Happy Trails Horror")
            assertThat(thing.type).isEqualTo(ThingType.BOARD_GAME_EXPANSION)
            assertThat(thing.names).hasSize(5)
            assertThat(thing.description)
                .startsWith("Final Girl Feature Film Box&#10;&#10;Summer camp ")
            assertThat(thing.yearPublished).isEqualTo(2021)
            assertThat(thing.minPlayers).isEqualTo(1)
            assertThat(thing.maxPlayers).isEqualTo(1)
            assertThat(thing.playingTimeInMinutes).isEqualTo(60)
            assertThat(thing.minPlayingTimeInMinutes).isEqualTo(20)
            assertThat(thing.maxPlayingTimeInMinutes).isEqualTo(60)
            assertThat(thing.minAge).isEqualTo(14)
            assertThat(thing.thumbnail).contains("T6zgfc99T9uq0RVqsqxdmFDuhEo")
            assertThat(thing.image).contains("Duq3Zkvlajxqsy7Vh1scYKru70M")
            assertThat(thing.links).hasSize(30)
            assertThat(thing.videos).hasSize(7)
            assertThat(thing.versions).hasSize(5)
            assertThat(thing.comments?.totalItems).isEqualTo(1238)
            assertThat(thing.comments?.comments).hasSize(100)
            assertThat(thing.statistics?.ratings?.usersRated).isEqualTo(1236) // Bug in BGG?
            assertThat(thing.listings).hasSize(3)
        }
    }

    @Nested
    inner class BoardGameAccessory {
        @Test
        fun `Parses all available data`() {
            val things =
                mapper.readValue(
                    TestUtils.xml(
                        "thing?id=307689&stats=1&ratingcomments=1&versions=1&marketplace=1&videos=1"
                    ),
                    Things::class.java,
                )

            assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            assertThat(thing.name).isEqualTo("Final Girl: Play Mat Set")
            assertThat(thing.type).isEqualTo(ThingType.BOARD_GAME_ACCESSORY)
            assertThat(thing.names).hasSize(2)
            assertThat(thing.description)
                .startsWith("A high quality game mat bundle that comes with two game mats")
            assertThat(thing.yearPublished).isEqualTo(2021)
            assertThat(thing.thumbnail).contains("Iv24VIJgXkwqMDQSbrb1HBt3gWI")
            assertThat(thing.image).contains("eI0-anIUPjq9MyHxAjsu54HaZHs=")
            assertThat(thing.links).hasSize(2)
            assertThat(thing.videos).hasSize(1)
            assertThat(thing.versions).hasSize(2)
            assertThat(thing.comments?.totalItems).isEqualTo(22)
            assertThat(thing.comments?.comments).hasSize(22)
            assertThat(thing.statistics?.ratings?.usersRated).isEqualTo(22)
            assertThat(thing.listings).hasSize(3)
        }
    }

    @Nested
    inner class VideoGame {
        @Test
        fun `Parses all available data`() {
            val things =
                mapper.readValue(
                    TestUtils.xml(
                        "thing?id=140545&stats=1&ratingcomments=1&versions=1&marketplace=1&videos=1"
                    ),
                    Things::class.java,
                )

            assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            assertThat(thing.name).isEqualTo("Hearthstone: Heroes of Warcraft")
            assertThat(thing.type).isEqualTo(ThingType.VIDEO_GAME)
            assertThat(thing.names).hasSize(2)
            assertThat(thing.description)
                .startsWith("Sheathe your sword, draw your deck, and get ready for Hearthstone")
            assertThat(thing.yearPublished).isNull()
            assertThat(thing.releaseDate).isEqualTo(LocalDate.of(2014, 1, 21))
            assertThat(thing.thumbnail).contains("ppeDi-cEnHYG96vYa7T8gwnn7OI")
            assertThat(thing.image).contains("ft5vToSnwsCajOxHMaSuKgl_IL4")
            assertThat(thing.links).hasSize(38)
            assertThat(thing.videos).hasSize(10)
            assertThat(thing.versions).hasSize(21)
            assertThat(thing.comments?.totalItems).isEqualTo(319)
            assertThat(thing.comments?.comments).hasSize(100)
            assertThat(thing.statistics?.ratings?.usersRated).isEqualTo(307)
            assertThat(thing.listings).hasSize(1)
        }
    }

    @Nested
    inner class RpgItem {
        @Test
        fun `Parses all available data`() {
            val things =
                mapper.readValue(
                    TestUtils.xml(
                        "thing?id=311654&stats=1&ratingcomments=1&versions=1&marketplace=1&videos=1"
                    ),
                    Things::class.java,
                )

            assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            assertThat(thing.name).isEqualTo("Alice is Missing")
            assertThat(thing.type).isEqualTo(ThingType.RPG_ITEM)
            assertThat(thing.names).hasSize(2)
            assertThat(thing.seriesCode).isEqualTo("")
            assertThat(thing.description)
                .startsWith("From the kickstarter:&#10;&#10;Alice is Missing")
            assertThat(thing.yearPublished).isEqualTo(2020)
            assertThat(thing.thumbnail).contains("g9QMstNAY2uOVSR1rH5Hx7Gxquk")
            assertThat(thing.image).contains("nGOu9LrFF5udRimbJrei6HExde8")
            assertThat(thing.links).hasSize(16)
            assertThat(thing.videos).hasSize(6)
            assertThat(thing.versions).hasSize(0)
            assertThat(thing.comments?.totalItems).isEqualTo(57)
            assertThat(thing.comments?.comments).hasSize(57)
            assertThat(thing.statistics?.ratings?.usersRated).isEqualTo(56)
            assertThat(thing.listings).hasSize(3)
        }
    }

    @Nested
    inner class RpgIssue {
        @Test
        fun `Parses all available data`() {
            val things =
                mapper.readValue(
                    TestUtils.xml("thing?id=51651&stats=1&ratingcomments=1&videos=1&marketplace=1"),
                    Things::class.java,
                )

            assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            assertThat(thing.name).isEqualTo("Dragon Magazine Archive")
            assertThat(thing.type).isEqualTo(ThingType.RPG_ISSUE)
            assertThat(thing.names).hasSize(1)
            assertThat(thing.description)
                .startsWith("A boxed set of 5 Data CD's that includes up to")
            assertThat(thing.yearPublished).isNull()
            assertThat(thing.datePublished).isEqualTo("1999-00-00")
            assertThat(thing.issueIndex).isEqualTo(5000)
            assertThat(thing.thumbnail).contains("DueVd0hHrr37r4WDXcQZnZks19E")
            assertThat(thing.image).contains("H4w1nkrD9nSW4lkWGaYH3kFPOY0")
            assertThat(thing.links).hasSize(17)
            assertThat(thing.videos).hasSize(0)
            assertThat(thing.versions).hasSize(0)
            assertThat(thing.comments?.totalItems).isEqualTo(27)
            assertThat(thing.comments?.comments).hasSize(27)
            assertThat(thing.statistics?.ratings?.usersRated).isEqualTo(26)
            assertThat(thing.listings).hasSize(0)
        }
    }
}
