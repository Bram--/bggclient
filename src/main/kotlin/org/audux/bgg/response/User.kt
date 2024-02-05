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

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDate

/**
 * Response wrapper for User response, contains user information and optionally lists of buddies,
 * guilds, top games and hot games.
 */
@JsonRootName("user")
data class User(
    /** Terms of use of the BGG API. */
    @JacksonXmlProperty(isAttribute = true) val termsOfUse: String,

    /** User's unique ID. */
    @JacksonXmlProperty(isAttribute = true) val id: Number?,

    /** User's name (username). */
    @JacksonXmlProperty(isAttribute = true) val name: String,

    /** User's first name. */
    val firstName: WrappedValue<String>,

    /** User's last name. */
    val lastName: WrappedValue<String>,

    /** User's web address/URL to their avatar or "N/A". */
    val avatarLink: WrappedValue<String>,

    /** The year the user registered on BGG or its network sites (e.g. rpggeek etx.) */
    val yearRegistered: WrappedValue<Number>?,

    /** Date the user last logged in */
    val lastLogin: WrappedValue<LocalDate>?,

    /** The state or province the user lives - if provided. */
    val stateOrProvince: WrappedValue<String>?,

    /** The country the user lives - if provided. */
    val country: WrappedValue<String>?,

    /** The user's website - if provided. */
    val webAddress: WrappedValue<String>?,

    /** The user's xBox account name - if provided. */
    val xBoxAccount: WrappedValue<String>,

    /** The user's Nintendo/Wii account name - if provided. */
    val wiiAccount: WrappedValue<String>,

    /** The user's PSN account name - if provided. */
    val psnAccount: WrappedValue<String>,

    /** The user's BattleNet account name - if provided. */
    val battleNetAccount: WrappedValue<String>,

    /** The user's Steam account name - if provided. */
    val steamAccount: WrappedValue<String>?,

    /** The user's aggregated rating for the marketplace. */
    val tradeRating: WrappedValue<Number>?,
    val buddies: Buddies?,
    val guilds: Guilds?,
    val top: Top?,
    val hot: Hot?,
)

data class Buddies(
    val total: Number,
    val page: Number,
    @JacksonXmlProperty(localName = "buddy") val buddies: List<Buddy>,
)

data class Buddy(
    @JacksonXmlProperty(isAttribute = true) val id: Number,
    @JacksonXmlProperty(isAttribute = true) val name: String
)

data class Guilds(
    val total: Number,
    val page: Number,
    @JsonProperty("guild") val guilds: List<Guild>,
)

data class Guild(
    @JacksonXmlProperty(isAttribute = true) val id: Number,
    @JacksonXmlProperty(isAttribute = true) val name: String,
)

data class Hot(
    @JacksonXmlProperty(isAttribute = true) val domain: String,
    @JsonProperty("item") val items: List<ListItem>,
)

data class Top(
    @JacksonXmlProperty(isAttribute = true) val domain: String,
    @JsonProperty("item") val items: List<ListItem>,
)

data class ListItem(
    @JacksonXmlProperty(isAttribute = true) val rank: Number,
    @JacksonXmlProperty(isAttribute = true) val type: String,
    @JacksonXmlProperty(isAttribute = true) val id: Number,
    @JacksonXmlProperty(isAttribute = true) val name: String,
)
