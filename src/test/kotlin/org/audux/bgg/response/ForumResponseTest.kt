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
import org.audux.bgg.InternalBggClient
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.test.KoinTest

/** Unit test for [Forum] data classes. */
class ForumResponseTest : KoinTest {
    private val mapper: ObjectMapper = InternalBggClient().mapper

    @Test
    fun `Parses empty response`() {
        // API wrongfully returns HTML when an invalid forum id is requested.
        val exception =
            assertThrows { mapper.readValue(TestUtils.xml("forum?id=-1"), Forum::class.java) }
                as Exception

        assertThat(exception).hasMessageThat().contains("Unexpected character")
    }

    @Test
    fun `Parses the forum for the given thing`() {
        val results = mapper.readValue(TestUtils.xml("forum?id=3696796"), Forum::class.java)

        val formatter = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss Z")
        assertThat(results.threads).hasSize(50)
        assertThat(results.threads[0])
            .isEqualTo(
                ThreadSummary(
                    id = 3202992,
                    subject =
                        "Solo AI Variant \"Haven\" - Competitive bot - Compact, quick, low maintenance - VIDEO TUTORIAL",
                    author = "cell141",
                    numArticles = 29,
                    postDate = LocalDateTime.parse("Tue, 05 Dec 2023 17:37:32 +0000", formatter),
                    lastPostDate = LocalDateTime.parse("Tue, 23 Jan 2024 12:33:44 +0000", formatter)
                )
            )
    }
}
