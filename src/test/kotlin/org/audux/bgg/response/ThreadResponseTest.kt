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
import com.google.common.truth.Truth
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.audux.bgg.module.BggXmlObjectMapper
import org.audux.bgg.module.appModule
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

class ThreadResponseTest : KoinTest {
    @JvmField
    @RegisterExtension
    @Suppress("unused")
    val koinTestExtension = KoinTestExtension.create { modules(appModule) }

    private val mapper: ObjectMapper by inject(named<BggXmlObjectMapper>())

    @Test
    fun `Parses empty response`() {
        assertThrows { mapper.readValue(TestUtils.xml("thread?id=0"), Thread::class.java) }
            as Exception
    }

    @Test
    fun `Parses the thread for the given thing`() {
        val thread = mapper.readValue(TestUtils.xml("thread"), Thread::class.java)

        Truth.assertThat(thread.subject).isEqualTo("New Maps for Ark Nova + Marine World")
        Truth.assertThat(thread.numArticles).isEqualTo(13)
        Truth.assertThat(thread.link).isEqualTo("https://boardgamegeek.com/thread/3208373")
        Truth.assertThat(thread.articles).hasSize(13)
        val firstArticle = thread.articles[0]
        Truth.assertThat(firstArticle.id).isEqualTo(43461362)
        Truth.assertThat(firstArticle.username).isEqualTo("darkuss")
        Truth.assertThat(firstArticle.numEdits).isEqualTo(4)
        Truth.assertThat(firstArticle.link)
            .isEqualTo("https://boardgamegeek.com/thread/3208373/article/43461362#43461362")
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssz")
        Truth.assertThat(firstArticle.postDate)
            .isEqualTo(LocalDateTime.parse("2023-12-15T13:07:50-06:00", formatter))
        Truth.assertThat(firstArticle.editDate)
            .isEqualTo(LocalDateTime.parse("2023-12-16T03:19:58-06:00", formatter))
        Truth.assertThat(firstArticle.subject).isEqualTo("New Maps for Ark Nova + Marine World")
        Truth.assertThat(firstArticle.body).hasLength(6133)
    }
}
