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
import java.time.LocalDateTime
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.audux.bgg.common.SubType
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** Unit test for [GeekList] data classes. */
class GeekListResponseTest {
    private val mapper: ObjectMapper = TestUtils.getBggClientMapper()

    @Test
    fun `Parses empty response`() {
        assertThrows<Exception> {
            mapper.readValue(TestUtils.xml("geeklist?id=0"), GeekList::class.java)
        }
    }

    @Test
    fun `is (K)Serializable`() {
        val geekList = mapper.readValue(TestUtils.xml("geeklist?id=331520"), GeekList::class.java)
        val encodedGeekList = Json.encodeToString(geekList)

        assertThat(Json.decodeFromString<GeekList>(encodedGeekList)).isEqualTo(geekList)
    }

    @Test
    fun `Parses a Geek list`() {
        val results = mapper.readValue(TestUtils.xml("geeklist?id=331520"), GeekList::class.java)

        assertThat(results.id).isEqualTo(331520)
        assertThat(results.postDate).isEqualTo(LocalDateTime.of(2024, 2, 6, 16, 45, 12))
        assertThat(results.editDate).isEqualTo(LocalDateTime.of(2024, 2, 6, 17, 5, 7))
        assertThat(results.thumbs).isEqualTo(63)
        assertThat(results.username).isEqualTo("Rawes6")
        assertThat(results.title).isEqualTo("10 Tips for Gaming with a Brain Injury")
        assertThat(results.description).hasLength(803)
        assertThat(results.comments).isEmpty()
        assertThat(results.numItems).isEqualTo(10)
        assertThat(results.items).hasSize(10)

        assertThat(results.items[0])
            .isEqualTo(
                GeekListItem(
                    id = 10493517,
                    postDate = LocalDateTime.of(2024, 2, 6, 15, 20, 58),
                    editDate = LocalDateTime.of(2024, 2, 6, 15, 20, 58),
                    imageId = 0,
                    objectId = 10119,
                    objectName = "Memory Madness",
                    objectType = "thing",
                    subType = SubType.BOARD_GAME,
                    thumbs = 6,
                    username = "Rawes6",
                    body =
                        "Listen, you have got to accept it, your noggin don't work the same as it" +
                            " used to. Your gonna forget rules, what games you've played, what" +
                            " you did on previous turns; hell, your gonna forget if you even" +
                            " liked the games sometimes. Use BGG to help supplement your memory." +
                            " Rank games right after playing, add notes to remember specific" +
                            " things and thoughts, and keep your collection up to date! It" +
                            " only takes a couple of games not added to easily forget you even" +
                            " have a game.",
                )
            )
    }

    @Test
    fun `Parses a Geek list including comments`() {
        val results =
            mapper.readValue(TestUtils.xml("geeklist?id=331520&comments=1"), GeekList::class.java)

        assertThat(results.id).isEqualTo(331520)
        assertThat(results.title).isEqualTo("10 Tips for Gaming with a Brain Injury")
        assertThat(results.comments).hasSize(10)
        val firstComment =
            GeekListComment(
                    username = "TomandJonna",
                    thumbs = 2,
                    date = LocalDateTime.of(2024, 2, 7, 14, 47, 55),
                    postDate = LocalDateTime.of(2024, 2, 7, 14, 47, 55),
                    editDate = LocalDateTime.of(2024, 2, 7, 14, 47, 55),
                )
                .also { it.value = "Thank you" }
        assertThat(results.comments[0]).isEqualTo(firstComment)
        assertThat(results.items[9].comments).hasSize(4)
    }
}
