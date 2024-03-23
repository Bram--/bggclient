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
import java.time.format.DateTimeFormatter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/** Unit test for [Forum] data classes. */
class ForumResponseTest {
    private val mapper: ObjectMapper = TestUtils.getBggClientMapper()

    @Test
    fun `Parses empty response`() {
        // API wrongfully returns HTML when an invalid forum id is requested.
        val exception =
            assertThrows { mapper.readValue(TestUtils.xml("forum?id=-1"), Forum::class.java) }
                as Exception

        assertThat(exception).hasMessageThat().contains("Unexpected character")
    }

    @Test
    fun `is (K)Serializable`() {
        val forum = mapper.readValue(TestUtils.xml("forum?id=3696796"), Forum::class.java)
        val encodedForum = Json.encodeToString(forum)

        assertThat(Json.decodeFromString<Forum>(encodedForum)).isEqualTo(forum)
    }

    @Test
    fun `Parses the forum for the given thing`() {
        val results = mapper.readValue(TestUtils.xml("forum?id=3696796"), Forum::class.java)

        val formatter = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss Z")
        assertThat(results.threads).hasSize(50)
        assertThat(results.threads[0])
            .isEqualTo(
                ThreadSummary(
                    id = 3244901,
                    subject = "Limited aquarium",
                    author = "Farouke",
                    numArticles = 11,
                    postDate = LocalDateTime.parse("Sun, 11 Feb 2024 13:15:58 +0000", formatter),
                    lastPostDate = LocalDateTime.parse("Wed, 14 Feb 2024 22:44:14 +0000", formatter)
                )
            )
    }
}
