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
package org.audux.bgg.data.request

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendPathSegments
import io.ktor.util.StringValues
import org.audux.bgg.BggClient
import org.audux.bgg.BggRequestException
import org.audux.bgg.data.response.Things

/**
 * The different kind/type of things the API may return such as a board game or expansion etc.
 * [See docs for more info](https://boardgamegeek.com/wiki/page/BGG_XML_API2#Thing_Items).
 */
enum class ThingType(val param: String) {
    BOARD_GAME("boardgame"),
    BOARD_GAME_EXPANSION("boardgameexpansion"),
    BOARD_GAME_ACCESSORY("boardgameaccessory"),
    VIDEO_GAME("videogame"),
    RPG_ITEM("rpgitem"),
    RPG_ISSUE("rpgissue")
}

/**
 * Request a Thing or list of things. Multiple things can be requested by passing in several IDs.
 * At least one ID is required to make this request. Sending along [types] might result in an empty
 * as the API filters based on the [ThingType].
 */
suspend fun BggClient.things(
    /**
     * Specifies the id of the thing(s) to retrieve. To request multiple things with a single
     * query, can specify a comma-delimited list of ids.
     */
    ids: Array<Int>,

    /**
     * Specifies that, regardless of the type of thing asked for by id, the results are
     * filtered by the THINGTYPE(s) specified. Multiple THINGTYPEs can be specified in
     * a comma-delimited list. Leave empty to return all types.
     * @see ThingType
     */
    types: Array<ThingType> = arrayOf(),

    /** Returns ranking and rating stats for the thing. */
    stats: Boolean = false,

    /** Returns version info for the thing. */
    versions: Boolean = false,

    /** Returns videos for the thing. */
    videos: Boolean = false,

    /** Returns marketplace data. */
    marketplace: Boolean = false,

    /**
     * Returns all comments about the thing. Also includes ratings when commented.
     * See page parameter.
     */
    comments: Boolean = false,

    /**
     * Returns all ratings for the thing. Also includes comments when rated. See page parameter.
     * The [ratingComments] and [comments] parameters cannot be used together, as the output
     * always appears in the <comments> node of the XML; comments parameter takes precedence if
     * both are specified.
     *
     * Ratings are sorted in descending rating value, based on the highest rating they have
     * assigned to that thing (each thing in the collection can have a different rating).
     */
    ratingComments: Boolean = false,

    /** Defaults to 1, controls the page of data to see for comments, and ratings data. */
    page: Int = 0,

    /**
     * Set the number of records to return in paging. Minimum is 10, maximum is 100.
     * Defaults to 100
     */
    pageSize: Int = 0,
): Things {
    if (!(10..100).contains(pageSize)) {
        throw BggRequestException("pageSize must be between 10 and 100")
    }
    if (comments && ratingComments) {
        throw BggRequestException("comments and ratingsComments can't both be true.")
    }

    val response = client.get(BggClient.BASE_URL) {
        url {
            appendPathSegments(BggClient.PATH_THING)

            parameters.appendAll(
                StringValues.build {
                    append(BggClient.PARAM_ID, ids.joinToString(","))

                    if (types.isNotEmpty()) {
                        append(BggClient.PARAM_TYPE, types.joinToString { "${it.param}," })
                    }

                    if (stats) append(BggClient.PARAM_STATS, "1")
                    if (versions) append(BggClient.PARAM_VERSIONS, "1")
                    if (videos) append(BggClient.PARAM_VIDEOS, "1")
                    if (marketplace) append(BggClient.PARAM_MARKETPLACE, "1")
                    if (comments) append(BggClient.PARAM_COMMENTS, "1")
                    if (ratingComments) append(BggClient.PARAM_RATING_COMMENTS, "1")
                    if (page > 0) append(BggClient.PARAM_PAGE, page.toString())
                    if (pageSize > 0) append(BggClient.PARAM_PAGE_SIZE, pageSize.toString())
                }
            )
        }
    }

    return mapper.readValue(response.bodyAsText(), Things::class.java)
}
