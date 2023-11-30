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
