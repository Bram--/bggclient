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
import org.audux.bgg.common.Name
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

class SearchResponseTest : KoinTest {
    @JvmField
    @RegisterExtension
    @Suppress("unused")
    val koinTestExtension = KoinTestExtension.create { modules(appModule) }

    private val mapper: ObjectMapper by inject(named<BggXmlObjectMapper>())

    @Test
    fun `Parses empty response`() {
        val results =
            mapper.readValue(TestUtils.xml("search?query=no+results"), SearchResults::class.java)

        Truth.assertThat(results.total).isEqualTo(0)
        Truth.assertThat(results.results).hasSize(0)
    }

    @Test
    fun `Parses search results`() {
        val results =
            mapper.readValue(TestUtils.xml("search?query=my+little"), SearchResults::class.java)

        Truth.assertThat(results.total).isEqualTo(144)
        Truth.assertThat(results.results).hasSize(144)

        Truth.assertThat(results.results[0])
            .isEqualTo(
                SearchResult(
                    id = 167159,
                    name = Name("Connect 4: My Little Pony", "primary"),
                    type = ThingType.BOARD_GAME,
                    yearPublished = WrappedValue(2014)
                )
            )
    }
}
