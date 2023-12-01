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
package org.audux.bgg.data.common

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

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

/** Encapsulates the name of a Thing either primary or alternate name. */
data class Name(
    /** The actual name. */
    @JacksonXmlProperty(isAttribute = true) val value: String,

    /** The type either: `primary` or `alternate`. */
    @JacksonXmlProperty(isAttribute = true) val type: String,

    /**
     * The order the names are displayed on the website. NOTE that primary and alternate might have
     * overlapping indexes.
     */
    @JacksonXmlProperty(isAttribute = true) val sortIndex: Int? = null,
)

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
    VIDEO_GAME_COMPANY("videogamecompany"); // Does not actually work?
}
