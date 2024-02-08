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

    /** The list of the User's buddies/friends. */
    val buddies: Buddies?,

    /** The list of guilds the user is a member of. */
    val guilds: Guilds?,

    /** The Top list made by the user on their profile. */
    val top: Top?,

    /** The Hot list made by the user on their profile. */
    val hot: Hot?,
)

/** List of Buddies/Friends of the user. */
data class Buddies(
    /** Total number of buddies. */
    val total: Number,

    /** The current page - used in pagination. */
    val page: Number,

    /** The actual list of buddies.. */
    @JacksonXmlProperty(localName = "buddy") val buddies: List<Buddy>,
)

/** Buddy of the user. */
data class Buddy(
    /** The id of the buddy. */
    @JacksonXmlProperty(isAttribute = true) val id: Number,

    /** The username of the buddy. */
    @JacksonXmlProperty(isAttribute = true) val name: String
)

/** Guilds user is a member of. */
data class Guilds(
    /** The total number of guilds user is a member of. */
    val total: Number,

    /** The current page - used in pagination. */
    val page: Number,

    /** The actual list of guilds */
    @JsonProperty("guild") val guilds: List<GuildReference>,
)

/** The id and name of a guild the user is a member of. */
data class GuildReference(
    /** The id of the guild. */
    @JacksonXmlProperty(isAttribute = true) val id: Number,

    /** The name of the guild. */
    @JacksonXmlProperty(isAttribute = true) val name: String,
)

/** The list of Hot items the user has added to their 'hot list' - as seen on their profile. */
data class Hot(
    /** The domain the hot list was specified on i.e. 'boardgame' - same as in the request. */
    @JacksonXmlProperty(isAttribute = true) val domain: String,

    /** The list of things that are part of the hot list. */
    @JsonProperty("item") val items: List<ListItem>,
)

/** The list of Top items the user has added to their 'top list' - as seen on their profile. */
data class Top(
    /** The domain the top list was specified on i.e. 'boardgame' - same as in the request. */
    @JacksonXmlProperty(isAttribute = true) val domain: String,

    /** The list of things that are part of the top list. */
    @JsonProperty("item") val items: List<ListItem>,
)

/**
 * Simple view of a item in the hot or top lists of the user, describing a boardgame, rpg etc. The
 * [org.audux.bgg.request.things] api may be used to request more information about the thing.
 */
data class ListItem(
    /** The (local) rank of the thin, i.e. the rank of the item in the user's hot or top list. */
    @JacksonXmlProperty(isAttribute = true) val rank: Number,

    /** The type of item/thing i.e. 'thing' */
    @JacksonXmlProperty(isAttribute = true) val type: String,

    /** The id of the item/thing. */
    @JacksonXmlProperty(isAttribute = true) val id: Number,

    /** The (primary) name of the item/thing. */
    @JacksonXmlProperty(isAttribute = true) val name: String,
)
