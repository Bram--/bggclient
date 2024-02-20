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
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test

/** Unit test for [Hot] data classes. */
class HotResponseTest {
    private val mapper: ObjectMapper = TestUtils.getBggClientMapper()

    @Test
    fun `Parses empty response`() {
        val results = mapper.readValue(TestUtils.xml("hot?type=rpgperson"), HotList::class.java)

        assertThat(results.results).hasSize(0)
    }

    @Test
    fun `Parses a hot list`() {
        val results = mapper.readValue(TestUtils.xml("hot"), HotList::class.java)

        assertThat(results.results).hasSize(50)
        assertThat(results.results[0])
            .isEqualTo(
                HotListItem(
                    id = 332686,
                    rank = 1,
                    name = "John Company: Second Edition",
                    yearPublished = 2022,
                    thumbnail =
                        "https://cf.geekdo-images.com/TAdE4z_bwAAjJlmPrkmKhA__thumb/img/pwgtQn8ArKjwBxk3bnDuIVAPWgU=/fit-in/200x150/filters:strip_icc()/pic6601629.jpg"
                )
            )
    }
}
