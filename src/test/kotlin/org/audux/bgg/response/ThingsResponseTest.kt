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

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.truth.Truth
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import org.audux.bgg.common.ThingType
import org.audux.bgg.module.BggXmlObjectMapper
import org.audux.bgg.module.appModule
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

/** Test class for [Things] and nested response classes. */
class ThingsResponseTest : KoinTest {
    @JvmField
    @RegisterExtension
    @Suppress("unused")
    val koinTestExtension = KoinTestExtension.create { modules(appModule) }

    private val mapper: ObjectMapper by inject(named<BggXmlObjectMapper>())

    @Test
    fun `parses an empty response`() {
        val things = mapper.readValue(TestUtils.xml("thing?id=-1"), Things::class.java)

        Truth.assertThat(things.things).hasSize(0)
    }

    @Test
    fun `parses multiple things`() {
        val things = mapper.readValue(TestUtils.xml("thing?id=1,2,3"), Things::class.java)

        Truth.assertThat(things.things).hasSize(3)
    }

    @Nested
    inner class BoardGame {
        @Test
        fun `parses a simple response`() {
            val things = mapper.readValue(TestUtils.xml("thing?id=1"), Things::class.java)

            Truth.assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            Truth.assertThat(thing.name).isEqualTo("Die Macher")
            Truth.assertThat(thing.type).isEqualTo(ThingType.BOARD_GAME)
            Truth.assertThat(thing.names).hasSize(3)
            Truth.assertThat(thing.description).hasLength(1270)
            WrappedValueSubject.assertThat(thing.yearPublished).hasValue(1986)
            WrappedValueSubject.assertThat(thing.minPlayers).hasValue(3)
            WrappedValueSubject.assertThat(thing.maxPlayers).hasValue(5)
            WrappedValueSubject.assertThat(thing.playingTimeInMinutes).hasValue(240)
            WrappedValueSubject.assertThat(thing.minPlayingTimeInMinutes).hasValue(240)
            WrappedValueSubject.assertThat(thing.maxPlayingTimeInMinutes).hasValue(240)
            WrappedValueSubject.assertThat(thing.minAge).hasValue(14)
        }

        @Test
        fun `parses polls`() {
            val things = mapper.readValue(TestUtils.xml("thing?id=1"), Things::class.java)

            Truth.assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            Truth.assertThat(thing.polls).hasSize(3)
            Truth.assertThat(thing.polls.map { it.javaClass })
                .containsExactly(
                    PlayerAgePoll::class.java,
                    LanguageDependencePoll::class.java,
                    NumberOfPlayersPoll::class.java
                )
        }

        @Test
        fun `parses poll - PlayerAgePoll`() {
            val things = mapper.readValue(TestUtils.xml("thing?id=1"), Things::class.java)

            val thing = things.things[0]
            val playerAgePoll = thing.polls.findLast { it is PlayerAgePoll }!! as PlayerAgePoll
            Truth.assertThat(playerAgePoll.title).isEqualTo("User Suggested Player Age")
            Truth.assertThat(playerAgePoll.totalVotes).isEqualTo(31)
            val groupedVotes = playerAgePoll.results
            Truth.assertThat(groupedVotes).hasSize(12)
            Truth.assertThat(groupedVotes[0].value).isEqualTo("2")
            Truth.assertThat(groupedVotes[0].numberOfVotes).isEqualTo(0)
            Truth.assertThat(groupedVotes[1].value).isEqualTo("3")
            Truth.assertThat(groupedVotes[1].numberOfVotes).isEqualTo(0)
            Truth.assertThat(groupedVotes[2].value).isEqualTo("4")
            Truth.assertThat(groupedVotes[2].numberOfVotes).isEqualTo(0)
            Truth.assertThat(groupedVotes[3].value).isEqualTo("5")
            Truth.assertThat(groupedVotes[3].numberOfVotes).isEqualTo(0)
            Truth.assertThat(groupedVotes[4].value).isEqualTo("6")
            Truth.assertThat(groupedVotes[4].numberOfVotes).isEqualTo(0)
            Truth.assertThat(groupedVotes[5].value).isEqualTo("8")
            Truth.assertThat(groupedVotes[5].numberOfVotes).isEqualTo(0)
            Truth.assertThat(groupedVotes[6].value).isEqualTo("10")
            Truth.assertThat(groupedVotes[6].numberOfVotes).isEqualTo(0)
            Truth.assertThat(groupedVotes[7].value).isEqualTo("12")
            Truth.assertThat(groupedVotes[7].numberOfVotes).isEqualTo(6)
            Truth.assertThat(groupedVotes[8].value).isEqualTo("14")
            Truth.assertThat(groupedVotes[8].numberOfVotes).isEqualTo(18)
            Truth.assertThat(groupedVotes[9].value).isEqualTo("16")
            Truth.assertThat(groupedVotes[9].numberOfVotes).isEqualTo(4)
            Truth.assertThat(groupedVotes[10].value).isEqualTo("18")
            Truth.assertThat(groupedVotes[10].numberOfVotes).isEqualTo(2)
            Truth.assertThat(groupedVotes[11].value).isEqualTo("21 and up")
            Truth.assertThat(groupedVotes[11].numberOfVotes).isEqualTo(1)
        }

        @Test
        fun `parses poll - LanguageDependencePoll`() {
            val things = mapper.readValue(TestUtils.xml("thing?id=1"), Things::class.java)

            val thing = things.things[0]
            val languageDependencePoll =
                thing.polls.findLast { it is LanguageDependencePoll }!! as LanguageDependencePoll
            Truth.assertThat(languageDependencePoll.title).isEqualTo("Language Dependence")
            Truth.assertThat(languageDependencePoll.totalVotes).isEqualTo(48)
            val groupedVotes = languageDependencePoll.results
            Truth.assertThat(groupedVotes[0].level).isEqualTo(1)
            Truth.assertThat(groupedVotes[0].numberOfVotes).isEqualTo(36)
            Truth.assertThat(groupedVotes[0].value).isEqualTo("No necessary in-game text")
            Truth.assertThat(groupedVotes[1].level).isEqualTo(2)
            Truth.assertThat(groupedVotes[1].numberOfVotes).isEqualTo(5)
            Truth.assertThat(groupedVotes[1].value)
                .isEqualTo("Some necessary text - easily memorized or small crib sheet")
            Truth.assertThat(groupedVotes[2].level).isEqualTo(3)
            Truth.assertThat(groupedVotes[2].numberOfVotes).isEqualTo(7)
            Truth.assertThat(groupedVotes[2].value)
                .isEqualTo("Moderate in-game text - needs crib sheet or paste ups")
            Truth.assertThat(groupedVotes[3].level).isEqualTo(4)
            Truth.assertThat(groupedVotes[3].numberOfVotes).isEqualTo(0)
            Truth.assertThat(groupedVotes[3].value)
                .isEqualTo("Extensive use of text - massive conversion needed to be playable")
            Truth.assertThat(groupedVotes[4].level).isEqualTo(5)
            Truth.assertThat(groupedVotes[4].numberOfVotes).isEqualTo(0)
            Truth.assertThat(groupedVotes[4].value).isEqualTo("Unplayable in another language")
        }

        @Test
        fun `parses poll - NumberOfPlayersPoll`() {
            val things = mapper.readValue(TestUtils.xml("thing?id=1"), Things::class.java)

            val thing = things.things[0]
            val numberOfPlayersPoll =
                thing.polls.findLast { it is NumberOfPlayersPoll }!! as NumberOfPlayersPoll
            Truth.assertThat(numberOfPlayersPoll.title)
                .isEqualTo("User Suggested Number of Players")
            Truth.assertThat(numberOfPlayersPoll.totalVotes).isEqualTo(136)

            val groupedVotes = numberOfPlayersPoll.results
            Truth.assertThat(groupedVotes[0].numberOfPlayers).isEqualTo("1")
            Truth.assertThat(groupedVotes[0].results[0].value).isEqualTo("Best")
            Truth.assertThat(groupedVotes[0].results[0].numberOfVotes).isEqualTo(0)
            Truth.assertThat(groupedVotes[0].results[1].value).isEqualTo("Recommended")
            Truth.assertThat(groupedVotes[0].results[1].numberOfVotes).isEqualTo(1)
            Truth.assertThat(groupedVotes[0].results[2].value).isEqualTo("Not Recommended")
            Truth.assertThat(groupedVotes[0].results[2].numberOfVotes).isEqualTo(86)

            Truth.assertThat(groupedVotes[1].numberOfPlayers).isEqualTo("2")
            Truth.assertThat(groupedVotes[1].results[0].value).isEqualTo("Best")
            Truth.assertThat(groupedVotes[1].results[0].numberOfVotes).isEqualTo(0)
            Truth.assertThat(groupedVotes[1].results[1].value).isEqualTo("Recommended")
            Truth.assertThat(groupedVotes[1].results[1].numberOfVotes).isEqualTo(1)
            Truth.assertThat(groupedVotes[1].results[2].value).isEqualTo("Not Recommended")
            Truth.assertThat(groupedVotes[1].results[2].numberOfVotes).isEqualTo(88)

            Truth.assertThat(groupedVotes[2].numberOfPlayers).isEqualTo("3")
            Truth.assertThat(groupedVotes[2].results[0].value).isEqualTo("Best")
            Truth.assertThat(groupedVotes[2].results[0].numberOfVotes).isEqualTo(2)
            Truth.assertThat(groupedVotes[2].results[1].value).isEqualTo("Recommended")
            Truth.assertThat(groupedVotes[2].results[1].numberOfVotes).isEqualTo(26)
            Truth.assertThat(groupedVotes[2].results[2].value).isEqualTo("Not Recommended")
            Truth.assertThat(groupedVotes[2].results[2].numberOfVotes).isEqualTo(76)

            Truth.assertThat(groupedVotes[3].numberOfPlayers).isEqualTo("4")
            Truth.assertThat(groupedVotes[3].results[0].value).isEqualTo("Best")
            Truth.assertThat(groupedVotes[3].results[0].numberOfVotes).isEqualTo(25)
            Truth.assertThat(groupedVotes[3].results[1].value).isEqualTo("Recommended")
            Truth.assertThat(groupedVotes[3].results[1].numberOfVotes).isEqualTo(86)
            Truth.assertThat(groupedVotes[3].results[2].value).isEqualTo("Not Recommended")
            Truth.assertThat(groupedVotes[3].results[2].numberOfVotes).isEqualTo(9)

            Truth.assertThat(groupedVotes[4].numberOfPlayers).isEqualTo("5")
            Truth.assertThat(groupedVotes[4].results[0].value).isEqualTo("Best")
            Truth.assertThat(groupedVotes[4].results[0].numberOfVotes).isEqualTo(116)
            Truth.assertThat(groupedVotes[4].results[1].value).isEqualTo("Recommended")
            Truth.assertThat(groupedVotes[4].results[1].numberOfVotes).isEqualTo(11)
            Truth.assertThat(groupedVotes[4].results[2].value).isEqualTo("Not Recommended")
            Truth.assertThat(groupedVotes[4].results[2].numberOfVotes).isEqualTo(2)

            Truth.assertThat(groupedVotes[5].numberOfPlayers).isEqualTo("5+")
            Truth.assertThat(groupedVotes[5].results[0].value).isEqualTo("Best")
            Truth.assertThat(groupedVotes[5].results[0].numberOfVotes).isEqualTo(1)
            Truth.assertThat(groupedVotes[5].results[1].value).isEqualTo("Recommended")
            Truth.assertThat(groupedVotes[5].results[1].numberOfVotes).isEqualTo(0)
            Truth.assertThat(groupedVotes[5].results[2].value).isEqualTo("Not Recommended")
            Truth.assertThat(groupedVotes[5].results[2].numberOfVotes).isEqualTo(62)
        }

        @Test
        fun `parses ratingcomments`() {
            val things =
                mapper.readValue(
                    TestUtils.xml(
                        "thing?id=396790&stats=1&ratingcomments=1&versions=1&marketplace=1&videos=1"
                    ),
                    Things::class.java
                )

            Truth.assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            Truth.assertThat(thing.comments?.page).isEqualTo(1)
            Truth.assertThat(thing.comments?.totalItems).isEqualTo(1402)
            Truth.assertThat(thing.comments?.comments).hasSize(100)
            Truth.assertThat(thing.comments?.comments?.get(0)?.rating).isEqualTo(10)
            Truth.assertThat(thing.comments?.comments?.get(0)?.value)
                .contains("This game is amazing")
            Truth.assertThat(thing.comments?.comments?.get(0)?.username).contains("actiondan87")

            val ratings = thing.comments?.comments?.map { it.rating }
            Truth.assertThat(ratings).hasSize(100)
            Truth.assertThat(ratings).containsExactlyElementsIn(Array(100) { 10 })
        }

        @Test
        fun `parses links`() {
            val things =
                mapper.readValue(
                    TestUtils.xml(
                        "thing?id=396790&stats=1&ratingcomments=1&versions=1&marketplace=1&videos=1"
                    ),
                    Things::class.java
                )

            Truth.assertThat(things.things).hasSize(1)
            val links = things.things[0].links
            Truth.assertThat(links).hasSize(33)
            Truth.assertThat(links[0].id).isEqualTo(1021)
            Truth.assertThat(links[0].inbound).isNull()
            Truth.assertThat(links[0].value).isEqualTo("Economic")
            Truth.assertThat(links[0].type).isEqualTo("boardgamecategory")
            Truth.assertThat(links.filter { it.type == "boardgamecategory" }).hasSize(3)
            Truth.assertThat(links.filter { it.type == "boardgamemechanic" }).hasSize(10)
            Truth.assertThat(links.filter { it.type == "boardgamefamily" }).hasSize(5)
            Truth.assertThat(links.filter { it.type == "boardgameexpansion" }).hasSize(2)
            Truth.assertThat(links.filter { it.type == "boardgamedesigner" }).hasSize(2)
            Truth.assertThat(links.filter { it.type == "boardgameartist" }).hasSize(3)
            Truth.assertThat(links.filter { it.type == "boardgamepublisher" }).hasSize(8)
        }

        @Test
        fun `parses versions`() {
            val things =
                mapper.readValue(
                    TestUtils.xml(
                        "thing?id=396790&stats=1&ratingcomments=1&versions=1&marketplace=1&videos=1"
                    ),
                    Things::class.java
                )

            Truth.assertThat(things.things).hasSize(1)
            val versions = things.things[0].versions
            Truth.assertThat(versions).hasSize(8)
            Truth.assertThat(versions[0].id).isEqualTo(685632)
            Truth.assertThat(versions[0].type).isEqualTo("boardgameversion")
            WrappedValueSubject.assertThat(versions[0].yearPublished).hasValue(2024)
            WrappedValueSubject.assertThat(versions[0].productCode).hasValue("8593085104517")
            WrappedValueSubject.assertThat(versions[0].width).hasValue(11.7)
            WrappedValueSubject.assertThat(versions[0].length).hasValue(11.7)
            WrappedValueSubject.assertThat(versions[0].depth).hasValue(2.8)
            WrappedValueSubject.assertThat(versions[0].weight).hasValue(0)
            Truth.assertThat(versions[0].thumbnail)
                .isEqualTo(
                    "https://cf.geekdo-images.com/D3TEgieEudyIiY70hkQiKw__thumb/img/DhEkGYrwkfDTAUWK7EFjwiVdBMU=/fit-in/200x150/filters:strip_icc()/pic7800352.png"
                )
            Truth.assertThat(versions[0].image)
                .isEqualTo(
                    "https://cf.geekdo-images.com/D3TEgieEudyIiY70hkQiKw__original/img/G9XIuskUuVU0he4ywThrz9bbpEA=/0x0/filters:format(png)/pic7800352.png"
                )
            Truth.assertThat(versions[0].names).hasSize(1)
            Truth.assertThat(versions[0].name).isEqualTo("Czech edition")

            val links = versions[0].links
            Truth.assertThat(links).hasSize(7)
        }

        @Test
        fun `parses statistics`() {
            val things =
                mapper.readValue(
                    TestUtils.xml(
                        "thing?id=396790&stats=1&ratingcomments=1&versions=1&marketplace=1&videos=1"
                    ),
                    Things::class.java
                )

            Truth.assertThat(things.things).hasSize(1)
            val stats = things.things[0].statistics!!
            val ratings = stats.ratings
            Truth.assertThat(stats.page).isEqualTo(1)
            WrappedValueSubject.assertThat(ratings.usersRated).hasValue(1373)
            WrappedValueSubject.assertThat(ratings.average).hasValue(8.27146)
            WrappedValueSubject.assertThat(ratings.bayesAverage).hasValue(6.57535)
            Truth.assertThat(ratings.ranks).hasSize(2)
            Truth.assertThat(ratings.ranks[0].type).isEqualTo("subtype")
            Truth.assertThat(ratings.ranks[0].id).isEqualTo(1)
            Truth.assertThat(ratings.ranks[0].name).isEqualTo("boardgame")
            Truth.assertThat(ratings.ranks[0].friendlyName).isEqualTo("Board Game Rank")
            Truth.assertThat(ratings.ranks[0].value).isEqualTo("1045")
            Truth.assertThat(ratings.ranks[0].bayesAverage).isEqualTo("6.57535")
            WrappedValueSubject.assertThat(ratings.stdDev).hasValue(1.25647)
            WrappedValueSubject.assertThat(ratings.median).hasValue(0)
            WrappedValueSubject.assertThat(ratings.owned).hasValue(2911)
            WrappedValueSubject.assertThat(ratings.trading).hasValue(9)
            WrappedValueSubject.assertThat(ratings.wanting).hasValue(381)
            WrappedValueSubject.assertThat(ratings.wishing).hasValue(3076)
            WrappedValueSubject.assertThat(ratings.numComments).hasValue(324)
            WrappedValueSubject.assertThat(ratings.numWeights).hasValue(173)
            WrappedValueSubject.assertThat(ratings.averageWeight).hasValue(4.0578)
        }

        @Test
        fun `parses marketplace listings`() {
            val things =
                mapper.readValue(
                    TestUtils.xml(
                        "thing?id=396790&stats=1&ratingcomments=1&versions=1&marketplace=1&videos=1"
                    ),
                    Things::class.java
                )

            Truth.assertThat(things.things).hasSize(1)
            val listings = things.things[0].listings
            Truth.assertThat(listings).hasSize(11)
            WrappedLocalDateTimeSubject.assertThat(listings[0].listDate)
                .hasValue(LocalDateTime.of(2023, 10, 6, 19, 41, 25))
            Truth.assertThat(listings[0].price.value).isEqualTo(80.00)
            Truth.assertThat(listings[0].price.currency).isEqualTo("USD")
            WrappedValueSubject.assertThat(listings[0].condition).hasValue("new")
            Truth.assertThat(listings[0].notes?.value).startsWith("Game is NIS")
            Truth.assertThat(listings[0].webLink.title).isEqualTo("marketlisting")
            Truth.assertThat(listings[0].webLink.href)
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
                    Things::class.java
                )

            Truth.assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            Truth.assertThat(thing.name).isEqualTo("Final Girl: The Happy Trails Horror")
            Truth.assertThat(thing.type).isEqualTo(ThingType.BOARD_GAME_EXPANSION)
            Truth.assertThat(thing.names).hasSize(5)
            Truth.assertThat(thing.description)
                .startsWith("Final Girl Feature Film Box&#10;&#10;Summer camp ")
            WrappedValueSubject.assertThat(thing.yearPublished).hasValue(2021)
            WrappedValueSubject.assertThat(thing.minPlayers).hasValue(1)
            WrappedValueSubject.assertThat(thing.maxPlayers).hasValue(1)
            WrappedValueSubject.assertThat(thing.playingTimeInMinutes).hasValue(60)
            WrappedValueSubject.assertThat(thing.minPlayingTimeInMinutes).hasValue(20)
            WrappedValueSubject.assertThat(thing.maxPlayingTimeInMinutes).hasValue(60)
            WrappedValueSubject.assertThat(thing.minAge).hasValue(14)
            Truth.assertThat(thing.thumbnail).contains("T6zgfc99T9uq0RVqsqxdmFDuhEo")
            Truth.assertThat(thing.image).contains("Duq3Zkvlajxqsy7Vh1scYKru70M")
            Truth.assertThat(thing.links).hasSize(30)
            Truth.assertThat(thing.videos).hasSize(7)
            Truth.assertThat(thing.versions).hasSize(5)
            Truth.assertThat(thing.comments?.totalItems).isEqualTo(1238)
            Truth.assertThat(thing.comments?.comments).hasSize(100)
            WrappedValueSubject.assertThat(thing.statistics?.ratings?.usersRated)
                .hasValue(1236) // Bug in BGG?
            Truth.assertThat(thing.listings).hasSize(3)
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
                    Things::class.java
                )

            Truth.assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            Truth.assertThat(thing.name).isEqualTo("Final Girl: Play Mat Set")
            Truth.assertThat(thing.type).isEqualTo(ThingType.BOARD_GAME_ACCESSORY)
            Truth.assertThat(thing.names).hasSize(2)
            Truth.assertThat(thing.description)
                .startsWith("A high quality game mat bundle that comes with two game mats")
            WrappedValueSubject.assertThat(thing.yearPublished).hasValue(2021)
            Truth.assertThat(thing.thumbnail).contains("Iv24VIJgXkwqMDQSbrb1HBt3gWI")
            Truth.assertThat(thing.image).contains("eI0-anIUPjq9MyHxAjsu54HaZHs=")
            Truth.assertThat(thing.links).hasSize(2)
            Truth.assertThat(thing.videos).hasSize(1)
            Truth.assertThat(thing.versions).hasSize(2)
            Truth.assertThat(thing.comments?.totalItems).isEqualTo(22)
            Truth.assertThat(thing.comments?.comments).hasSize(22)
            WrappedValueSubject.assertThat(thing.statistics?.ratings?.usersRated).hasValue(22)
            Truth.assertThat(thing.listings).hasSize(3)
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
                    Things::class.java
                )

            Truth.assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            Truth.assertThat(thing.name).isEqualTo("Hearthstone: Heroes of Warcraft")
            Truth.assertThat(thing.type).isEqualTo(ThingType.VIDEO_GAME)
            Truth.assertThat(thing.names).hasSize(2)
            Truth.assertThat(thing.description)
                .startsWith("Sheathe your sword, draw your deck, and get ready for Hearthstone")
            WrappedValueSubject.assertThat(thing.yearPublished).isNull()
            WrappedValueSubject.assertThat(thing.releaseDate).hasValue(LocalDate.of(2014, 1, 21))
            Truth.assertThat(thing.thumbnail).contains("ppeDi-cEnHYG96vYa7T8gwnn7OI")
            Truth.assertThat(thing.image).contains("ft5vToSnwsCajOxHMaSuKgl_IL4")
            Truth.assertThat(thing.links).hasSize(38)
            Truth.assertThat(thing.videos).hasSize(10)
            Truth.assertThat(thing.versions).hasSize(21)
            Truth.assertThat(thing.comments?.totalItems).isEqualTo(319)
            Truth.assertThat(thing.comments?.comments).hasSize(100)
            WrappedValueSubject.assertThat(thing.statistics?.ratings?.usersRated).hasValue(307)
            Truth.assertThat(thing.listings).hasSize(1)
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
                    Things::class.java
                )

            Truth.assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            Truth.assertThat(thing.name).isEqualTo("Alice is Missing")
            Truth.assertThat(thing.type).isEqualTo(ThingType.RPG_ITEM)
            Truth.assertThat(thing.names).hasSize(2)
            WrappedValueSubject.assertThat(thing.seriesCode).hasValue("")
            Truth.assertThat(thing.description)
                .startsWith("From the kickstarter:&#10;&#10;Alice is Missing")
            WrappedValueSubject.assertThat(thing.yearPublished).hasValue(2020)
            Truth.assertThat(thing.thumbnail).contains("g9QMstNAY2uOVSR1rH5Hx7Gxquk")
            Truth.assertThat(thing.image).contains("nGOu9LrFF5udRimbJrei6HExde8")
            Truth.assertThat(thing.links).hasSize(16)
            Truth.assertThat(thing.videos).hasSize(6)
            Truth.assertThat(thing.versions).hasSize(0)
            Truth.assertThat(thing.comments?.totalItems).isEqualTo(57)
            Truth.assertThat(thing.comments?.comments).hasSize(57)
            WrappedValueSubject.assertThat(thing.statistics?.ratings?.usersRated).hasValue(56)
            Truth.assertThat(thing.listings).hasSize(3)
        }
    }

    @Nested
    inner class RpgIssue {
        @Test
        fun `Parses all available data`() {
            val things =
                mapper.readValue(
                    TestUtils.xml("thing?id=51651&stats=1&ratingcomments=1&videos=1&marketplace=1"),
                    Things::class.java
                )

            Truth.assertThat(things.things).hasSize(1)
            val thing = things.things[0]
            Truth.assertThat(thing.name).isEqualTo("Dragon Magazine Archive")
            Truth.assertThat(thing.type).isEqualTo(ThingType.RPG_ISSUE)
            Truth.assertThat(thing.names).hasSize(1)
            Truth.assertThat(thing.description)
                .startsWith("A boxed set of 5 Data CD's that includes up to")
            WrappedValueSubject.assertThat(thing.yearPublished).isNull()
            WrappedValueSubject.assertThat(thing.datePublished).hasValue("1999-00-00")
            WrappedValueSubject.assertThat(thing.issueIndex).hasValue(5000)
            Truth.assertThat(thing.thumbnail).contains("DueVd0hHrr37r4WDXcQZnZks19E")
            Truth.assertThat(thing.image).contains("H4w1nkrD9nSW4lkWGaYH3kFPOY0")
            Truth.assertThat(thing.links).hasSize(17)
            Truth.assertThat(thing.videos).hasSize(0)
            Truth.assertThat(thing.versions).hasSize(0)
            Truth.assertThat(thing.comments?.totalItems).isEqualTo(27)
            Truth.assertThat(thing.comments?.comments).hasSize(27)
            WrappedValueSubject.assertThat(thing.statistics?.ratings?.usersRated).hasValue(26)
            Truth.assertThat(thing.listings).hasSize(0)
        }
    }

    @Test
    fun `WrappedValue Parses self closing elements - Int`() {
        val xml = """<item value="100" />""""
        val wrappedInt = mapper.readValue(xml, object : TypeReference<WrappedValue<Int>>() {})

        WrappedValueSubject.assertThat(wrappedInt).hasValue(100)
    }

    @Test
    fun `WrappedValue Parses self closing elements - String`() {
        val xml = """<item value="Hello" />""""
        val wrappedInt = mapper.readValue(xml, object : TypeReference<WrappedValue<String>>() {})

        WrappedValueSubject.assertThat(wrappedInt).hasValue("Hello")
    }

    @Test
    fun `WrappedLocalDateTime Parses BGG specific date format`() {
        val localDateXml = """<item value="Sun, 13 Sep 2020 10:43:49 +0000" />""""
        val localDateTime = mapper.readValue(localDateXml, WrappedLocalDateTime::class.java)

        WrappedLocalDateTimeSubject.assertThat(localDateTime)
            .hasValue(LocalDateTime.of(2020, 9, 13, 10, 43, 49))
    }
}
