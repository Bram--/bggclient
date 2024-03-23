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

/** Unit test for [ForumList] data classes. */
class ForumListResponseTest {
    private val mapper: ObjectMapper = TestUtils.getBggClientMapper()

    @Test
    fun `Parses empty response`() {
        val results = mapper.readValue(TestUtils.xml("forumlist?id=-1"), ForumList::class.java)

        assertThat(results.forums).hasSize(0)
    }

    @Test
    fun `is (K)Serializable`() {
        val forumList = mapper.readValue(TestUtils.xml("forumlist?id=-1"), ForumList::class.java)
        val encodedForumList = Json.encodeToString(forumList)

        assertThat(Json.decodeFromString<ForumList>(encodedForumList)).isEqualTo(forumList)
    }

    @Test
    fun `Parses a Forum list for the given thing`() {
        val results = mapper.readValue(TestUtils.xml("forumlist"), ForumList::class.java)

        val formatter = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss Z")
        assertThat(results.forums).hasSize(10)
        assertThat(results.forums)
            .containsExactly(
                ForumSummary(
                    id = 3696791,
                    groupId = 0,
                    title = "Reviews",
                    noPosting = false,
                    description =
                        "Post your game reviews in this forum.  <A href=\"/thread/59278\">Click here for help on writing game reviews.</A>",
                    numThreads = 65,
                    numPosts = 1603,
                    lastPostDate = LocalDateTime.parse("Tue, 23 Jan 2024 09:13:43 +0000", formatter)
                ),
                ForumSummary(
                    id = 3696792,
                    groupId = 0,
                    title = "Sessions",
                    noPosting = false,
                    description = "Post your session reports here.",
                    numThreads = 12,
                    numPosts = 99,
                    lastPostDate = LocalDateTime.parse("Sun, 14 Jan 2024 22:56:08 +0000", formatter)
                ),
                ForumSummary(
                    id = 3696793,
                    groupId = 0,
                    title = "General",
                    noPosting = false,
                    description = "Post any related article to this game here.",
                    numThreads = 633,
                    numPosts = 9271,
                    lastPostDate = LocalDateTime.parse("Wed, 24 Jan 2024 17:29:58 +0000", formatter)
                ),
                ForumSummary(
                    id = 3696794,
                    groupId = 0,
                    title = "Rules",
                    noPosting = false,
                    description = "Post any rules questions you have here.",
                    numThreads = 1096,
                    numPosts = 8191,
                    lastPostDate = LocalDateTime.parse("Tue, 23 Jan 2024 20:53:33 +0000", formatter)
                ),
                ForumSummary(
                    id = 3696795,
                    groupId = 0,
                    title = "Strategy",
                    noPosting = false,
                    description = "Post strategy and tactics articles here.",
                    numThreads = 119,
                    numPosts = 1853,
                    lastPostDate = LocalDateTime.parse("Tue, 23 Jan 2024 18:50:11 +0000", formatter)
                ),
                ForumSummary(
                    id = 3696796,
                    groupId = 0,
                    title = "Variants",
                    noPosting = false,
                    description = "Post variants to the game rules here.",
                    numThreads = 146,
                    numPosts = 2335,
                    lastPostDate = LocalDateTime.parse("Tue, 23 Jan 2024 12:33:44 +0000", formatter)
                ),
                ForumSummary(
                    id = 3696797,
                    groupId = 0,
                    title = "News",
                    noPosting = false,
                    description = "Post time sensitive announcements here.",
                    numThreads = 19,
                    numPosts = 726,
                    lastPostDate = LocalDateTime.parse("Fri, 19 Jan 2024 15:29:10 +0000", formatter)
                ),
                ForumSummary(
                    id = 3696798,
                    groupId = 0,
                    title = "Crowdfunding",
                    noPosting = false,
                    description = "Post crowdfunding / preorder content here.",
                    numThreads = 0,
                    numPosts = 0,
                    lastPostDate = null
                ),
                ForumSummary(
                    id = 3696799,
                    groupId = 0,
                    title = "Play By Forum",
                    noPosting = false,
                    description = "Run Play By Forum (PBF) games here.",
                    numThreads = 0,
                    numPosts = 0,
                    lastPostDate = null
                ),
                ForumSummary(
                    id = 3696800,
                    groupId = 0,
                    title = "Find Players",
                    noPosting = false,
                    description = "Post here to find local gamers and to promote local events.",
                    numThreads = 12,
                    numPosts = 228,
                    lastPostDate = LocalDateTime.parse("Mon, 22 Jan 2024 13:22:41 +0000", formatter)
                ),
            )
    }
}
