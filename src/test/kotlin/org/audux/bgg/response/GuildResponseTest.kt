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
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test

/** Unit test for [Guild] data classes. */
class GuildResponseTest {
    private val mapper: ObjectMapper = TestUtils.getBggClientMapper()

    @Test
    fun `Parses a guild`() {
        val results = mapper.readValue(TestUtils.xml("guilds?id=2310"), Guild::class.java)

        assertThat(results.id).isEqualTo(2310)
        assertThat(results.name).isEqualTo("St Albans Board Games Club")
        assertThat(results.createdAt).isEqualTo(LocalDateTime.of(2015, 7, 27, 13, 45, 48))
        assertThat(results.manager).isEqualTo("montoc1701")
        assertThat(results.website).isEqualTo("https://www.facebook.com/groups/StABoardgamesclub/")
        assertThat(results.category).isEqualTo("group")
        assertThat(results.description).hasLength(2_149)
        assertThat(results.members).isNull()
        assertThat(results.location)
            .isEqualTo(
                Location(
                    addressLine1 = "",
                    addressLine2 = "",
                    city = "St. Albans",
                    stateOrProvince = "Hertfordshire",
                    postalCode = "AL3",
                    country = "United Kingdom",
                )
            )
    }

    @Test
    fun `Parses a guild including members`() {
        val results =
            mapper.readValue(
                TestUtils.xml("guilds?id=2310&members=1&sort=date&page=0"),
                Guild::class.java
            )

        assertThat(results.id).isEqualTo(2310)
        assertThat(results.members?.count).isEqualTo(31)
        assertThat(results.members?.page).isEqualTo(1)
        assertThat(results.members!!.members[0])
            .isEqualTo(
                GuildMember(name = "Novaeux", joinDate = LocalDateTime.of(2024, 2, 1, 15, 2, 36))
            )
    }
}
