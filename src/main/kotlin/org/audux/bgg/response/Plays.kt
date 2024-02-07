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

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDate
import org.audux.bgg.common.PlayThingType

/** Response wrapper for plays by the user to be returned. */
@JsonRootName("plays")
data class Plays(
    /** Terms of use of the BGG API. */
    @JacksonXmlProperty(isAttribute = true) val termsOfUse: String,

    /** The ID of the user. */
    @JacksonXmlProperty(isAttribute = true) val userid: Number,

    /** The username, same as in the request. */
    @JacksonXmlProperty(isAttribute = true) val username: String,

    /** Total number of plays - used in pagination. */
    @JacksonXmlProperty(isAttribute = true) val total: Number,

    /** The current page number. */
    @JacksonXmlProperty(isAttribute = true) val page: Number,

    /** List of the actual plays. */
    @JacksonXmlProperty(localName = "play") val plays: List<Play>
)

/**
 * Represents a single(or batched as specified by `quantity`) play, including the 'thing'/game and
 * its players.
 */
data class Play(
    /** Unique ID of the play - not used */
    @JacksonXmlProperty(isAttribute = true) val id: Number,
    /** The date the play took place. */
    @JacksonXmlProperty(isAttribute = true) val date: LocalDate,
    /** The number of plays, of the same game with the same players. */
    @JacksonXmlProperty(isAttribute = true) val quantity: Number,
    /** The duration of the play(s). */
    @JacksonXmlProperty(isAttribute = true, localName = "length") val lengthInMinutes: Number,

    /** Whether the game was completed or not. */
    @JacksonXmlProperty(isAttribute = true)
    @JsonDeserialize(using = NumberToBooleanDeserializer::class)
    val incomplete: Boolean,

    /**
     * Whether to count these stats or not - When true this play should not be counted as a valid
     * game. I.e. game was played wrong, perhaps an introduction game was played etc.
     */
    @JacksonXmlProperty(isAttribute = true)
    @JsonDeserialize(using = NumberToBooleanDeserializer::class)
    val noWinStats: Boolean,

    /** The location the play took place. */
    @JacksonXmlProperty(isAttribute = true) val location: String?,

    /** The thing that was played with e.g. a board game, RPG etc. */
    val item: PlayItem,

    /** List of comments on the play. These are only comments made by the user logging the play. */
    val comments: List<String>,

    /** The players that partook. */
    @JacksonXmlElementWrapper(localName = "players") val players: List<Player>,
)

/** Represents the item/thing that was played e.g. a board game. */
data class PlayItem(
    /** The name of the item. */
    @JacksonXmlProperty(isAttribute = true) val name: String,

    /** The type of item/object e.g. thing or family. */
    @JacksonXmlProperty(isAttribute = true)
    @JsonDeserialize(using = PlayThingTypeDeserializer::class)
    val objectType: PlayThingType,

    /**
     * The unique identifier of the thing, used to retrieve more information using the
     * [org.audux.bgg.request.things] API.
     */
    @JacksonXmlProperty(isAttribute = true) val objectId: Number,

    /** The sub types for this thing e.g. board game, rpg etc. */
    @JacksonXmlElementWrapper(localName = "subtypes", useWrapping = false)
    val subTypes: List<SubType>,
)

/** A SubType of a tihng e.g. board game. */
data class SubType(val subtype: WrappedSubType)

/**
 * Represents a person in the play i.e. their username, id, what color they played, how they did
 * etc.
 */
data class Player(
    @JacksonXmlProperty(isAttribute = true) val username: String?,
    @JacksonXmlProperty(isAttribute = true) val userid: Number?,
    @JacksonXmlProperty(isAttribute = true) val name: String?,
    @JacksonXmlProperty(isAttribute = true) val startPosition: String?,
    @JacksonXmlProperty(isAttribute = true) val color: String?,
    @JacksonXmlProperty(isAttribute = true)
    @JsonDeserialize(using = NumberToBooleanDeserializer::class)
    val new: Boolean?,
    @JacksonXmlProperty(isAttribute = true) val rating: Number?,
    @JacksonXmlProperty(isAttribute = true)
    @JsonDeserialize(using = NumberToBooleanDeserializer::class)
    val win: Boolean?,
    @JacksonXmlProperty(isAttribute = true) val score: Number?,
)
