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
import org.audux.bgg.util.TestUtils
import org.audux.bgg.util.TestUtils.xml
import org.junit.jupiter.api.Test

/** Test class for [User] data classes. */
class UserResponseTest {
    private val mapper: ObjectMapper = TestUtils.getBggClientMapper()

    @Test
    fun `Parses empty response`() {
        val results = mapper.readValue(xml("user?name=NULL"), User::class.java)

        assertThat(results.name).isEmpty()
    }

    @Test
    fun `is (K)Serializable`() {
        val user =
            mapper.readValue(
                xml("user?name=Novaeux&buddies=1&hot=1&top=1&guilds=1&page=1&domain=boardgame"),
                User::class.java,
            )
        val encodedUser = Json.encodeToString(user)

        assertThat(Json.decodeFromString<User>(encodedUser)).isEqualTo(user)
    }

    @Test
    fun `Parses a user without extras`() {
        val results = mapper.readValue(xml("user?name=Novaeux"), User::class.java)

        assertThat(results.name).isEqualTo("Novaeux")
        assertThat(results.id).isEqualTo(2639010)
        assertThat(results.firstName).isEqualTo("Bram")
        assertThat(results.lastName).isEqualTo("Wijnands")
        assertThat(results.avatarLink).isEqualTo("N/A")
        assertThat(results.yearRegistered).isEqualTo(2020)
        assertThat(results.lastLogin).isEqualTo(LocalDate.of(2024, 2, 5))
        assertThat(results.stateOrProvince).isEqualTo("England")
        assertThat(results.country).isEqualTo("United Kingdom")
        assertThat(results.webAddress).isEqualTo("")
        assertThat(results.xBoxAccount).isEqualTo("")
        assertThat(results.wiiAccount).isEqualTo("")
        assertThat(results.psnAccount).isEqualTo("")
        assertThat(results.battleNetAccount).isEqualTo("")
        assertThat(results.steamAccount).isEqualTo("")
        assertThat(results.tradeRating).isEqualTo(0)
        assertThat(results.buddies).isNull()
        assertThat(results.guilds).isNull()
        assertThat(results.top).isNull()
        assertThat(results.hot).isNull()
    }

    @Test
    fun `Parses a user with all additional data`() {
        val results =
            mapper.readValue(
                xml("user?name=Novaeux&buddies=1&hot=1&top=1&guilds=1&page=1&domain=boardgame"),
                User::class.java,
            )

        assertThat(results.name).isEqualTo("Novaeux")
        assertThat(results.id).isEqualTo(2639010)
        assertThat(results.firstName).isEqualTo("Bram")
        assertThat(results.lastName).isEqualTo("Wijnands")
        assertThat(results.buddies?.total).isEqualTo(1)
        assertThat(results.guilds?.total).isEqualTo(2)
        assertThat(results.top?.items).hasSize(2)
        assertThat(results.hot?.items).hasSize(2)
    }
}
