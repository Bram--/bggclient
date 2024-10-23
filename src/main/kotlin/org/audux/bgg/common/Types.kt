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
package org.audux.bgg.common

import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import kotlinx.serialization.Serializable
import org.audux.bgg.response.WrappedDoubleDeserializer
import org.audux.bgg.response.WrappedIntDeserializer

/**
 * The different kind/type of things the API may return such as a board game or expansion etc.
 * [See docs for more info](https://boardgamegeek.com/wiki/page/BGG_XML_API2#Thing_Items).
 */
@Serializable
enum class ThingType(val param: String) {
    UNKNOWN(""), // Used whenever the type is empty or not recognized.
    BOARD_GAME("boardgame"),
    BOARD_GAME_ACCESSORY("boardgameaccessory"),
    BOARD_GAME_COMPILATION("boardgamecompilation"),
    BOARD_GAME_EXPANSION("boardgameexpansion"),
    BOARD_GAME_ISSUE("boardgameissue"),
    VIDEO_GAME("videogame"),
    VIDEO_GAME_COMPILATION("videogamecompilation"),
    VIDEO_GAME_EXPANSION("videogameexpansion"),
    VIDEO_GAME_HARDWARE("videogamehardware"),
    RPG_ITEM("rpgitem"),
    RPG_ISSUE("rpgissue");

    companion object {
        fun fromParam(param: String?) = entries.find { it.param == param } ?: UNKNOWN
    }
}

/**
 * Different type of hot lists that can be requested using the hot request.
 *
 * Note: About half of these types actually do _not_ return any list.
 */
@Serializable
enum class HotListType(val param: String) {
    UNKNOWN(""), // Used whenever the type is empty or not recognized.
    BOARD_GAME("boardgame"),
    BOARD_GAME_COMPANY("boardgamecompany"), // Does not actually work?
    BOARD_GAME_PERSON("boardgameperson"),
    RPG("rpg"),
    RPG_COMPANY("rpgcompany"), // Does not actually work?
    RPG_PERSON("rpgperson"), // Does not actually work?
    VIDEO_GAME("videogame"), // Does not actually work?
    VIDEO_GAME_COMPANY("videogamecompany"), // Does not actually work?
}

/** Different sub types returned in the [org.audux.bgg.request.plays] request/ response */
@Serializable
enum class SubType(val param: String) {
    UNKNOWN(""), // Used whenever the type is empty or not recognized.
    BOARD_GAME("boardgame"),
    BOARD_GAME_ACCESSORY("boardgameaccessory"),
    BOARD_GAME_COMPILATION("boardgamecompilation"),
    BOARD_GAME_EXPANSION("boardgameexpansion"),
    BOARD_GAME_INTEGRATION("boardgameintegration"),
    BOARD_GAME_IMPLEMENTATION("boardgameimplementation"),
    RPG("rpg"),
    RPG_ITEM("rpgitem"),
    VIDEO_GAME("videogame");

    companion object {
        fun fromParam(param: String?) = entries.find { it.param == param } ?: UNKNOWN
    }
}

/**
 * The different kind/type of families the API may return such as a board game or rpgs.
 * [See docs for more info](https://boardgamegeek.com/wiki/page/BGG_XML_API2#Family_Items).
 */
@Serializable
enum class FamilyType(val param: String) {
    UNKNOWN(""), // Used whenever the type is empty or not recognized.
    RPG("rpg"),
    RPG_PERIODICAL("rpgperiodical"),
    BOARD_GAME_FAMILY("boardgamefamily");

    companion object {
        fun fromParam(param: String?) = entries.find { it.param == param } ?: UNKNOWN
    }
}

/**
 * Used to map the id in the forumlist request to either a family ot thing.
 * [See docs for more info](https://boardgamegeek.com/wiki/page/BGG_XML_API2#Forum_Lists).
 */
@Serializable
enum class ForumListType(val param: String) {
    UNKNOWN(""), // Used whenever the type is empty or not recognized.
    THING("thing"),
    FAMILY("family");

    companion object {
        fun fromParam(param: String?) = entries.find { it.param == param } ?: UNKNOWN
    }
}

/** Used to show what type of thing it is when played, either a thing or a family(?) */
@Serializable
enum class PlayThingType(val param: String) {
    UNKNOWN(""), // Used whenever the type is empty or not recognized.
    THING("thing"),
    FAMILY("family");

    companion object {
        fun fromParam(param: String?) = entries.find { it.param == param } ?: UNKNOWN
    }
}

/**
 * Used to either include or exclude certain items in a request, see
 * [org.audux.bgg.request.collection] and [org.audux.bgg.request.user].
 */
@Serializable
enum class Inclusion {
    INCLUDE,
    EXCLUDE;

    fun toParam() = if (this == INCLUDE) "1" else "0"
}

/** Different domains used for the users' hot- and top-10. */
@Serializable
enum class Domain(val param: String, val address: String) {
    BOARD_GAME_GEEK("boardgame", "https://boardgamegeek.com"),
    RPG_GEEK("rpg", "https://rpggeek.com"),
    VIDEO_GAME_GEEK("videogame", "https://videogamegeek.com"),
}

/** Encapsulates the name of a Thing either primary or alternate name. */
@Serializable
data class Name(
    /** The actual name. */
    @JacksonXmlProperty(isAttribute = true) val value: String,

    /** The type either: `primary` or `alternate` - occasionally not set. */
    @JacksonXmlProperty(isAttribute = true) val type: String? = null,

    /**
     * The order the names are displayed on the website. NOTE that primary and alternate might have
     * overlapping indexes.
     */
    @JacksonXmlProperty(isAttribute = true) val sortIndex: Int? = null,
)

/** Wrapper for [Ratings]. */
@Serializable
data class Statistics(
    /** Unused attribute? */
    @JacksonXmlProperty(isAttribute = true) val page: Int?,

    /** The set of ratings */
    val ratings: Ratings,
)

/**
 * Contains rating aggregated and other statistics like average rating, standard deviation, number
 * of comments.
 */
@Serializable
data class Ratings(
    /** A user rating if available. */
    @JacksonXmlProperty(isAttribute = true) val value: String? = null,

    /** Int of user ratings. */
    @JsonDeserialize(using = WrappedIntDeserializer::class) val usersRated: Int? = null,

    /** The average rating. */
    @JsonDeserialize(using = WrappedDoubleDeserializer::class) val average: Double? = null,

    /** Standard deviation. */
    @JsonDeserialize(using = WrappedDoubleDeserializer::class) val stdDev: Double? = null,

    /** Bayesian average rating. */
    @JsonDeserialize(using = WrappedDoubleDeserializer::class) val bayesAverage: Double? = null,

    /** The median rating. */
    @JsonDeserialize(using = WrappedDoubleDeserializer::class) val median: Double? = null,

    /** Total number of users owning this thing. */
    @JsonDeserialize(using = WrappedIntDeserializer::class) val owned: Int? = null,

    /** Total number of users looking to trade away this thing. */
    @JsonDeserialize(using = WrappedIntDeserializer::class) val trading: Int? = null,

    /** Total number of users wanting this thing. */
    @JsonDeserialize(using = WrappedIntDeserializer::class) val wanting: Int? = null,

    /** Total number of users wishing for this thing. */
    @JsonDeserialize(using = WrappedIntDeserializer::class) val wishing: Int? = null,

    /** Total number of comments left on the thing. */
    @JsonDeserialize(using = WrappedIntDeserializer::class) val numComments: Int? = null,

    /** Int of weight ratings. */
    @JsonDeserialize(using = WrappedIntDeserializer::class) val numWeights: Int? = null,

    /** Average weight rating. */
    @JsonDeserialize(using = WrappedDoubleDeserializer::class) val averageWeight: Double? = null,

    /**
     * A thing can be listed on different rankings. For example a board game could both be ranked as
     * a board game and a strategy game.
     */
    @JacksonXmlElementWrapper(localName = "ranks") val ranks: List<Rank> = listOf(),
)

/** Represents a rank in a single ranking (Consisting of type & name). */
@Serializable
data class Rank(
    /** Unique of the ranking type - ID and type+name should always be a coupled. */
    @JacksonXmlProperty(isAttribute = true) val id: Int,

    /** Type of ranking e.g. the thing's main type or sub type */
    @JacksonXmlProperty(isAttribute = true) val type: String? = null,

    /** The name of the ranking e.g. "boardgame", "strategygame" etc. */
    @JacksonXmlProperty(isAttribute = true) val name: String,

    /** Friendly/Natural language name of the ranking. */
    @JacksonXmlProperty(isAttribute = true) val friendlyName: String,

    /** The actual rank of the thing in this ranking. Either a number or 'Not Ranked'. */
    @JacksonXmlProperty(isAttribute = true) val value: String? = null,

    /** It's bayesian average in this ranking. */
    @JacksonXmlProperty(isAttribute = true) val bayesAverage: String? = null,
)

/**
 * Describes a link or relationship to another class of object. For example a board game thing may
 * contain a list of links to a `boardgamemechanic` like `Income`, `Hand management`. Common types
 * are:
 * * boardgameartist
 * * boardgamecategory
 * * boardgamedesigner
 * * boardgameexpansion
 * * boardgamemechanic
 * * rpgitemartist
 * * rpgitemcategory
 * * rpgitemdesigner
 * * rpgitemexpansion
 * * rpgitemmechanic
 *
 *   And so on.
 */
@Serializable
data class Link(
    /**
     * The id for the link, most of these cannot be retrieved via the API although a 'family'-API
     * exists.
     */
    @JacksonXmlProperty(isAttribute = true) val id: Int,

    /** The unique name of the Link i.e. links with the same ID will always carry the same name. */
    @JacksonXmlProperty(isAttribute = true) val value: String,

    /** The type of the link as outlined in the class description. */
    @JacksonXmlProperty(isAttribute = true) val type: String,

    /** Direction of the Link. */
    @JacksonXmlProperty(isAttribute = true) val inbound: Boolean?,
)
