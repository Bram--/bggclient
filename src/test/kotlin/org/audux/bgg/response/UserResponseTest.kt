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
import org.audux.bgg.InternalBggClient
import org.audux.bgg.response.WrappedValueSubject.Companion.assertThat
import org.audux.bgg.util.TestUtils.xml
import org.junit.jupiter.api.Test

/** Test class for [User] data classes. */
class UserResponseTest {
    private val mapper: ObjectMapper = InternalBggClient().mapper

    @Test
    fun `Parses empty response`() {
        val results = mapper.readValue(xml("user?name=NULL"), User::class.java)

        assertThat(results.name).isEmpty()
    }

    @Test
    fun `Parses a user without extras`() {
        val results = mapper.readValue(xml("user?name=Novaeux"), User::class.java)

        assertThat(results.name).isEqualTo("Novaeux")
        assertThat(results.id).isEqualTo(2639010)
        assertThat(results.firstName).hasValue("Bram")
        assertThat(results.lastName).hasValue("Wijnands")
        assertThat(results.avatarLink).hasValue("N/A")
        assertThat(results.yearRegistered).hasValue(2020)
        assertThat(results.lastLogin).hasValue(LocalDate.of(2024, 2, 5))
        assertThat(results.stateOrProvince).hasValue("England")
        assertThat(results.country).hasValue("United Kingdom")
        assertThat(results.webAddress).hasValue("")
        assertThat(results.xBoxAccount).hasValue("")
        assertThat(results.wiiAccount).hasValue("")
        assertThat(results.psnAccount).hasValue("")
        assertThat(results.battleNetAccount).hasValue("")
        assertThat(results.steamAccount).hasValue("")
        assertThat(results.tradeRating).hasValue(0)
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
                User::class.java
            )

        assertThat(results.name).isEqualTo("Novaeux")
        assertThat(results.id).isEqualTo(2639010)
        assertThat(results.firstName).hasValue("Bram")
        assertThat(results.lastName).hasValue("Wijnands")
        assertThat(results.buddies?.total).isEqualTo(1)
        assertThat(results.guilds?.total).isEqualTo(2)
        assertThat(results.top?.items).hasSize(2)
        assertThat(results.hot?.items).hasSize(2)
    }
}
