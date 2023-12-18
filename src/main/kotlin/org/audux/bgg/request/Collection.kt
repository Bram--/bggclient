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
package org.audux.bgg.request

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendPathSegments
import io.ktor.util.StringValues
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.audux.bgg.BggClient
import org.audux.bgg.common.Inclusion
import org.audux.bgg.common.ThingType
import org.audux.bgg.request.Constants.BASE_URL
import org.audux.bgg.request.Constants.PARAM_BGG_RATING
import org.audux.bgg.request.Constants.PARAM_BRIEF
import org.audux.bgg.request.Constants.PARAM_COLLECTION_ID
import org.audux.bgg.request.Constants.PARAM_COMMENT
import org.audux.bgg.request.Constants.PARAM_EXCLUDE_SUBTYPE
import org.audux.bgg.request.Constants.PARAM_HAS_PARTS
import org.audux.bgg.request.Constants.PARAM_ID
import org.audux.bgg.request.Constants.PARAM_MAX_PLAYS
import org.audux.bgg.request.Constants.PARAM_MINIMUM_BGG_RATING
import org.audux.bgg.request.Constants.PARAM_MINIMUM_PLAYS
import org.audux.bgg.request.Constants.PARAM_MINIMUM_RATING
import org.audux.bgg.request.Constants.PARAM_MODIFIED_SINCE
import org.audux.bgg.request.Constants.PARAM_OWN
import org.audux.bgg.request.Constants.PARAM_PLAYED
import org.audux.bgg.request.Constants.PARAM_PREVIOUSLY_OWNED
import org.audux.bgg.request.Constants.PARAM_PRE_ORDERED
import org.audux.bgg.request.Constants.PARAM_RATED
import org.audux.bgg.request.Constants.PARAM_RATING
import org.audux.bgg.request.Constants.PARAM_STATS
import org.audux.bgg.request.Constants.PARAM_SUBTYPE
import org.audux.bgg.request.Constants.PARAM_TRADE
import org.audux.bgg.request.Constants.PARAM_USERNAME
import org.audux.bgg.request.Constants.PARAM_VERSION
import org.audux.bgg.request.Constants.PARAM_WANT
import org.audux.bgg.request.Constants.PARAM_WANT_PARTS
import org.audux.bgg.request.Constants.PARAM_WANT_TO_BUY
import org.audux.bgg.request.Constants.PARAM_WANT_TO_PLAY
import org.audux.bgg.request.Constants.PARAM_WISHLIST
import org.audux.bgg.request.Constants.PARAM_WISHLIST_PRIORITY
import org.audux.bgg.request.Constants.PATH_COLLECTION
import org.audux.bgg.response.Collection

fun BggClient.collection(
    /** Name of the user to request the collection for. */
    userName: String,

    /**
     * Specifies which collection you want to retrieve - only one type at the time.
     *
     * NOTE: When using [ThingType.BOARD_GAME] expansions are also returned but wrongly have the
     * type set to [ThingType.BOARD_GAME], to only retrieve boardgames set [excludeSubType] to
     * [ThingType.BOARD_GAME_EXPANSION].
     */
    subType: ThingType,

    /** Specifies which subtype you want to exclude from the results. */
    excludeSubType: ThingType? = null,

    /** Filter collection to specifically listed item(s). */
    ids: Array<Int>? = null,

    /** Returns version info for each item in your collection. */
    version: Boolean = false,

    /** Returns more abbreviated results. */
    brief: Boolean = false,

    /** Returns expanded rating/ranking info for the collection. */
    stats: Boolean = false,

    /**
     * Filter for owned games. Set to [Inclusion.EXCLUDE] to exclude these items so marked. Set to
     * [Inclusion.INCLUDE] for returning owned games and 0 for non-owned games.
     */
    own: Inclusion? = null,

    /**
     * Filter for whether an item has been rated. Set to [Inclusion.EXCLUDE] to exclude these items
     * so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
     */
    rated: Inclusion? = null,

    /**
     * Filter for whether an item has been played. Set to [Inclusion.EXCLUDE] to exclude these items
     * so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
     */
    played: Inclusion? = null,

    /**
     * Filter for items that have been commented. Set to [Inclusion.EXCLUDE] to exclude these items
     * so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
     */
    comment: Inclusion? = null,

    /**
     * Filter for items marked for trade. Set to [Inclusion.EXCLUDE] to exclude these items so
     * marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
     */
    trade: Inclusion? = null,

    /**
     * Filter for items wanted in trade. Set to [Inclusion.EXCLUDE] to exclude these items so
     * marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
     */
    want: Inclusion? = null,

    /**
     * Filter for items on the wishlist. Set to [Inclusion.EXCLUDE] to exclude these items so
     * marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
     */
    wishlist: Inclusion? = null,

    /** Filter for wishlist priority. Returns only items of the specified priority. Range 1-5. */
    wishlistPriority: Int? = null,

    /**
     * Filter for pre-ordered games. Set to [Inclusion.EXCLUDE] to exclude these items so marked.
     * Set to [Inclusion.INCLUDE] to include only these items so marked.
     */
    preOrdered: Inclusion? = null,

    /**
     * Filter for items marked as wanting to play. Set to [Inclusion.EXCLUDE] to exclude these items
     * so marked. Set to [Inclusion.EXCLUDE] to include only these items so marked.
     */
    wantToPlay: Inclusion? = null,

    /**
     * Filter for ownership flag. Set to [Inclusion.EXCLUDE] to exclude these items so marked. Set
     * to [Inclusion.INCLUDE] to include only these items so marked.
     */
    wantToBuy: Inclusion? = null,

    /**
     * Filter for games marked previously owned. Set to [Inclusion.EXCLUDE] to exclude these items
     * so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
     */
    previouslyOwned: Inclusion? = null,

    /**
     * Filter on whether there is a comment in the Has Parts field of the item. Set to
     * [Inclusion.EXCLUDE] to exclude these items so marked. Set to [Inclusion.INCLUDE] to include
     * only these items so marked.
     */
    hasParts: Inclusion? = null,

    /**
     * Filter on whether there is a comment in the Wants Parts field of the item. Set to
     * [Inclusion.EXCLUDE] to exclude these items so marked. Set to [Inclusion.INCLUDE] to include
     * only these items so marked.
     */
    wantParts: Inclusion? = null,

    /** Filter on minimum personal rating assigned for that item in the collection. Range 1-10. */
    minRating: Int? = null,

    /** Filter on maximum personal rating assigned for that item in the collection. Rang 1-10 */
    rating: Int? = null,

    /**
     * Filter on minimum BGG rating for that item in the collection. Range 1-10 (Or -1).
     *
     * Note: 0 is ignored. You can use -1 e.g. min -1 and max 1 to get items with no bgg rating.
     */
    minBggRating: Int? = null,

    /** Filter on maximum BGG rating for that item in the collection. Range 1-10. */
    bggRating: Int? = null,

    /** Filter by minimum number of recorded plays. */
    minimumPlays: Int? = null,

    /** Filter by maximum number of recorded plays. */
    maxPlays: Int? = null,

    /**
     * Restrict the collection results to the single specified collection id. `Collid` is returned
     * in the results of normal queries as well.
     */
    collectionId: Int? = null,

    /**
     * Restricts the collection results to only those whose status (own, want, fortrade, etc.) has
     * changed or been added since the date specified (does not return results for deletions). Time
     * may be added as well: modifiedsince=YY-MM-DD%20HH:MM:SS
     */
    modifiedSince: LocalDateTime? = null,
): Request<Collection> {
    return request {
        val response =
            client.get(BASE_URL) {
                url {
                    appendPathSegments(PATH_COLLECTION)
                    parameters.appendAll(
                        StringValues.build {
                            append(PARAM_USERNAME, userName)
                            append(PARAM_SUBTYPE, subType.param)

                            excludeSubType?.let { append(PARAM_EXCLUDE_SUBTYPE, it.param) }
                            ids?.let { append(PARAM_ID, it.joinToString(",")) }
                            if (version) append(PARAM_VERSION, "1")
                            if (brief) append(PARAM_BRIEF, "1")
                            if (stats) append(PARAM_STATS, "1")
                            own?.let { append(PARAM_OWN, it.toParam()) }
                            rated?.let { append(PARAM_RATED, it.toParam()) }
                            played?.let { append(PARAM_PLAYED, it.toParam()) }
                            comment?.let { append(PARAM_COMMENT, it.toParam()) }
                            trade?.let { append(PARAM_TRADE, it.toParam()) }
                            want?.let { append(PARAM_WANT, it.toParam()) }
                            wishlist?.let { append(PARAM_WISHLIST, it.toParam()) }
                            wishlistPriority?.let { append(PARAM_WISHLIST_PRIORITY, it.toString()) }
                            preOrdered?.let { append(PARAM_PRE_ORDERED, it.toParam()) }
                            wantToPlay?.let { append(PARAM_WANT_TO_PLAY, it.toParam()) }
                            wantToBuy?.let { append(PARAM_WANT_TO_BUY, it.toParam()) }
                            previouslyOwned?.let { append(PARAM_PREVIOUSLY_OWNED, it.toParam()) }
                            hasParts?.let { append(PARAM_HAS_PARTS, it.toParam()) }
                            wantParts?.let { append(PARAM_WANT_PARTS, it.toParam()) }
                            minRating?.let { append(PARAM_MINIMUM_RATING, it.toString()) }
                            rating?.let { append(PARAM_RATING, it.toString()) }
                            minBggRating?.let { append(PARAM_MINIMUM_BGG_RATING, it.toString()) }
                            bggRating?.let { append(PARAM_BGG_RATING, it.toString()) }
                            minimumPlays?.let { append(PARAM_MINIMUM_PLAYS, it.toString()) }
                            maxPlays?.let { append(PARAM_MAX_PLAYS, it.toString()) }
                            collectionId?.let { append(PARAM_COLLECTION_ID, it.toString()) }
                            modifiedSince?.let {
                                val formatter = DateTimeFormatter.ofPattern("yy-MM-dd HH:mm:ss")
                                append(PARAM_MODIFIED_SINCE, formatter.format(modifiedSince))
                            }
                        }
                    )
                }
            }

        mapper.readValue(response.bodyAsText(), Collection::class.java)
    }
}
