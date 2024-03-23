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
import java.time.LocalDate
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.audux.bgg.common.PlayThingType
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test

/** Unit test for [Plays] data classes. */
class PlaysResponseTest {
    private val mapper: ObjectMapper = TestUtils.getBggClientMapper()

    @Test
    fun `Parses empty response`() {
        val results = mapper.readValue(TestUtils.xml("plays?username=null"), Plays::class.java)

        assertThat(results.username).isEqualTo("null")
        assertThat(results.plays).isEmpty()
    }

    @Test
    fun `is (K)Serializable`() {
        val plays = mapper.readValue(TestUtils.xml("plays?username=Novaeux"), Plays::class.java)
        val encodedPlays = Json.encodeToString(plays)

        assertThat(Json.decodeFromString<Plays>(encodedPlays)).isEqualTo(plays)
    }

    @Test
    fun `Parses a list of plays`() {
        val results = mapper.readValue(TestUtils.xml("plays?username=Novaeux"), Plays::class.java)

        assertThat(results.username).isEqualTo("Novaeux")
        assertThat(results.userid).isEqualTo(2639010)
        assertThat(results.total).isEqualTo(6)
        assertThat(results.page).isEqualTo(1)
        assertThat(results.plays).hasSize(6)
        val civGame = results.plays[1]
        assertThat(civGame.id).isEqualTo(58563743)
        assertThat(civGame.date).isEqualTo(LocalDate.of(2022, 2, 19))
        assertThat(civGame.quantity).isEqualTo(1)
        assertThat(civGame.lengthInMinutes).isEqualTo(380)
        assertThat(civGame.incomplete).isFalse()
        assertThat(civGame.noWinStats).isFalse()
        assertThat(civGame.location).isEqualTo("3rd St Albans Scouts")
        assertThat(civGame.comments)
            .isEqualTo(listOf("China, Japan, India game. I won as Gandhi with a culture victory."))
        assertThat(civGame.item)
            .isEqualTo(
                PlayItem(
                    name = "Sid Meier's Civilization: The Board Game",
                    objectType = PlayThingType.THING,
                    objectId = 77130,
                    subTypes = listOf(SubType(org.audux.bgg.common.SubType.BOARD_GAME))
                )
            )
        assertThat(civGame.players)
            .containsExactly(
                Player(
                    username = "Novaeux",
                    userid = 2639010,
                    name = "Bram Wijnands",
                    startPosition = "",
                    new = false,
                    rating = 0.0,
                    win = true,
                    color = "Purple"
                ),
                Player(
                    username = "",
                    userid = 0,
                    name = "Redacted",
                    startPosition = "",
                    new = false,
                    rating = 0.0,
                    win = false,
                    color = "Yellow"
                ),
                Player(
                    username = "",
                    userid = 0,
                    name = "Redacted",
                    startPosition = "",
                    color = "",
                    new = false,
                    rating = 0.0,
                    win = false
                ),
            )
    }
}
