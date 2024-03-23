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

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDateTime
import kotlinx.serialization.Serializable
import org.audux.bgg.common.Constants

/** Response wrapper for Guilds to be returned. */
@JsonRootName("guild")
@Serializable
data class Guild(
    /** Terms of use of the BGG API. */
    @JacksonXmlProperty(isAttribute = true) val termsOfUse: String,

    /** Unique identifier for the guild, same as in the request. */
    @JacksonXmlProperty(isAttribute = true) val id: Int,

    /** Name of the guild/group. */
    @JacksonXmlProperty(isAttribute = true) val name: String?,

    /** The date and time the guild was created. */
    @JsonFormat(pattern = Constants.DAY_FIRST_DATE_TIME_FORMAT)
    @JacksonXmlProperty(isAttribute = true, localName = "created")
    @Serializable(with = LocalDateTimeSerializer::class)
    val createdAt: LocalDateTime?,

    /** The category of the Guild e.g. an interest group, regional group etc. */
    val category: String?,

    /** The external website of the Guild */
    val website: String?,

    /** The username of the Guild's manager. */
    val manager: String?,

    /** Description of the guild. */
    val description: String?,

    /** Physical location of the guild. */
    val location: Location?,

    /** List of guild members. */
    val members: GuildMembers?,
)

/** The physical location of the guild. */
@Serializable
data class Location(
    /** First address line. */
    @JsonProperty("addr1") val addressLine1: String,

    /** Second address line. */
    @JsonProperty("addr2") val addressLine2: String,

    /** The city the guild resides in. */
    val city: String,

    /** The state or province the guild resides in. */
    val stateOrProvince: String,

    /** The postal code of the guild's location. */
    val postalCode: String,

    /** The country the guild resides in. */
    val country: String,
)

/** Represents a (partial) list of members of the guild. */
@Serializable
data class GuildMembers(
    /** The total number of guild members. */
    @JacksonXmlProperty(isAttribute = true) val count: Int,

    /**
     * The current page of guild members - 25 members per page/GuildMembers object.
     *
     * @see org.audux.bgg.request.PaginatedGuilds
     */
    @JacksonXmlProperty(isAttribute = true) val page: Int,

    /** The actual list of guild members. */
    @JsonProperty("member") val members: List<GuildMember>,
)

/**
 * A GuildMember entry, consisting of simply a username (can be used to retrieve more user info) and
 * a join date and time.
 */
@Serializable
data class GuildMember(
    /** The username of the guild member. */
    @JacksonXmlProperty(isAttribute = true) val name: String,

    /** When the user joined the guild. */
    @JsonFormat(pattern = Constants.DAY_FIRST_DATE_TIME_FORMAT)
    @JacksonXmlProperty(isAttribute = true, localName = "date")
    @Serializable(with = LocalDateTimeSerializer::class)
    val joinDate: LocalDateTime,
)
