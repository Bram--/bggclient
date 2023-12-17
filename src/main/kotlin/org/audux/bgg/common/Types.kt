/**
 * Copyright 2023 Bram Wijnands
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

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import org.audux.bgg.response.WrappedValue

/**
 * The different kind/type of things the API may return such as a board game or expansion etc.
 * [See docs for more info](https://boardgamegeek.com/wiki/page/BGG_XML_API2#Thing_Items).
 */
enum class ThingType(val param: String) {
    UNKNOWN(""), // Used whenever the type is empty or not recognized.
    BOARD_GAME("boardgame"),
    BOARD_GAME_EXPANSION("boardgameexpansion"),
    BOARD_GAME_ACCESSORY("boardgameaccessory"),
    VIDEO_GAME("videogame"),
    RPG_ITEM("rpgitem"),
    RPG_ISSUE("rpgissue");

    companion object {
        fun fromParam(param: String?) = values().find { it.param == param } ?: UNKNOWN
    }
}

/**
 * Different type of hot lists that can be requested using the hot request.
 *
 * Note: About half of these types actually do _not_ return any list.
 */
enum class HotListType(val param: String) {
    UNKNOWN(""), // Used whenever the type is empty or not recognized.
    BOARD_GAME("boardgame"),
    BOARD_GAME_COMPANY("boardgamecompany"), // Does not actually work?
    BOARD_GAME_PERSON("boardgameperson"),
    RPG("rpg"),
    RPG_COMPANY("rpgcompany"), // Does not actually work?
    RPG_PERSON("rpgperson"), // Does not actually work?
    VIDEO_GAME("videogame"), // Does not actually work?
    VIDEO_GAME_COMPANY("videogamecompany") // Does not actually work?
}

/**
 * Used to either include or exclude certain items in a request, see
 * [org.audux.bgg.data.request.collection].
 */
enum class Inclusion {
    INCLUDE,
    EXCLUDE;

    fun toParam() = if (this == INCLUDE) "1" else "0"
}

/** Encapsulates the name of a Thing either primary or alternate name. */
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

// region Statistics and ratings.
/** Wrapper for [Ratings]. */
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
data class Ratings(
    /** A user rating if available. */
    @JacksonXmlProperty(isAttribute = true) val value: String? = null,

    /** Number of user ratings. */
    val usersRated: WrappedValue<Number>? = null,

    /** The average rating. */
    val average: WrappedValue<Number>? = null,

    /** Standard deviation. */
    val stdDev: WrappedValue<Number>? = null,

    /** Bayesian average rating. */
    val bayesAverage: WrappedValue<Number>? = null,

    /** The median rating. */
    val median: WrappedValue<Number>? = null,

    /** Total number of users owning this thing. */
    val owned: WrappedValue<Number>? = null,

    /** Total number of users looking to trade away this thing. */
    val trading: WrappedValue<Number>? = null,

    /** Total number of users wanting this thing. */
    val wanting: WrappedValue<Number>? = null,

    /** Total number of users wishing for this thing. */
    val wishing: WrappedValue<Number>? = null,

    /** Total number of comments left on the thing. */
    val numComments: WrappedValue<Number>? = null,

    /** Number of weight ratings. */
    val numWeights: WrappedValue<Number>? = null,

    /** Average weight rating. */
    val averageWeight: WrappedValue<Number>? = null,

    /**
     * A thing can be listed on different rankings. For example a board game could both be ranked as
     * a board game and a strategy game.
     */
    @JacksonXmlElementWrapper(localName = "ranks") val ranks: List<Rank> = listOf(),
)

/** Represents a rank in a single ranking (Consisting of type & name). */
data class Rank(
    /** Unique of the ranking type - ID and type+name should always be a coupled. */
    @JacksonXmlProperty(isAttribute = true) val id: Number,

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
// endregion
