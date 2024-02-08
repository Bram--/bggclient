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
package org.audux.bgg.request

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendPathSegments
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
import org.audux.bgg.request.Constants.REQUEST_DATE_TIME_FORMAT
import org.audux.bgg.response.Collection

/**
 * Request details about a user's collection.
 *
 * <p>NOTE: The default (or using [subType]=[ThingType.BOARD_GAME]) returns both
 * [ThingType.BOARD_GAME] and [ThingType.BOARD_GAME_EXPANSION] in the collection... BUT incorrectly
 * marks the [subType] as [ThingType.BOARD_GAME] for the expansions. Workaround is to use
 * [excludeSubType]=[ThingType.BOARD_GAME_EXPANSION] and make a 2nd call asking for
 * [subType]=[ThingType.BOARD_GAME_EXPANSION]
 *
 * @param userName Name of the user to request the collection for
 * @param subType Specifies which collection you want to retrieve - only one type at the time
 * @param excludeSubType Specifies which subtype you want to exclude from the results.
 * @param ids Filter collection to specifically listed item(s).
 * @param version Returns version info for each item in your collection.
 * @param brief Returns more abbreviated results.
 * @param stats Returns expanded rating/ranking info for the collection.
 * @param own Filter for owned games. Set to [Inclusion.EXCLUDE] to exclude these items so marked.
 *   Set to [Inclusion.INCLUDE] for returning owned games and 0 for non-owned games.
 * @param rated Filter for whether an item has been rated. Set to [Inclusion.EXCLUDE] to exclude
 *   these items so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
 * @param played Filter for whether an item has been played. Set to [Inclusion.EXCLUDE] to exclude
 *   these items so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
 * @param comment Filter for items that have been commented. Set to [Inclusion.EXCLUDE] to exclude
 *   these items so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
 * @param trade Filter for items marked for trade. Set to [Inclusion.EXCLUDE] to exclude these items
 *   so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
 * @param want Filter for items wanted in trade. Set to [Inclusion.EXCLUDE] to exclude these items
 *   so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
 * @param wishlist Filter for items on the wishlist. Set to [Inclusion.EXCLUDE] to exclude these
 *   items so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
 * @param wishlistPriority Filter for wishlist priority. Returns only items of the specified
 *   priority. Range 1-5.
 * @param preOrdered Filter for pre-ordered games. Set to [Inclusion.EXCLUDE] to exclude these items
 *   so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
 * @param wantToPlay Filter for items marked as wanting to play. Set to [Inclusion.EXCLUDE] to
 *   exclude these items so marked. Set to [Inclusion.EXCLUDE] to include only these items so
 *   marked.
 * @param wantToBuy Filter for ownership flag. Set to [Inclusion.EXCLUDE] to exclude these items so
 *   marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
 * @param previouslyOwned Filter for games marked previously owned. Set to [Inclusion.EXCLUDE] to
 *   exclude these items so marked. Set to [Inclusion.INCLUDE] to include only these items so
 *   marked.
 * @param hasParts Filter on whether there is a comment in the Has Parts field of the item. Set to
 *   [Inclusion.EXCLUDE] to exclude these items so marked. Set to [Inclusion.INCLUDE] to include
 *   only these items so marked.
 * @param wantParts Filter on whether there is a comment in the Wants Parts field of the item. Set
 *   to [Inclusion.EXCLUDE] to exclude these items so marked. Set to [Inclusion.INCLUDE] to include
 *   only these items so marked.
 * @param minRating Filter on minimum personal rating assigned for that item in the collection.
 *   Range 1-10.
 * @param rating Filter on maximum personal rating assigned for that item in the collection. Rang
 *   1-10
 * @param minBggRating Filter on minimum BGG rating for that item in the collection. Range 1-10 (Or
 *   -1). NOTE: 0 is ignored. You can use -1 e.g. min -1 and max 1 to get items with no bgg rating.
 * @param bggRating Filter on maximum BGG rating for that item in the collection. Range 1-10.
 * @param minimumPlays Filter by minimum number of recorded plays.
 * @param maxPlays Filter by maximum number of recorded plays.
 * @param collectionId Restrict the collection results to the single specified collection id.
 *   `Collid` is returned in the results of normal queries as well.
 * @param modifiedSince Restricts the collection results to only those whose status (own, want,
 *   fortrade, etc.) has changed or been added since the date specified (does not return results for
 *   deletions).
 */
fun BggClient.collection(
    userName: String,
    subType: ThingType,
    excludeSubType: ThingType? = null,
    ids: Array<Int>? = null,
    version: Boolean = false,
    brief: Boolean = false,
    stats: Boolean = false,
    own: Inclusion? = null,
    rated: Inclusion? = null,
    played: Inclusion? = null,
    comment: Inclusion? = null,
    trade: Inclusion? = null,
    want: Inclusion? = null,
    wishlist: Inclusion? = null,
    wishlistPriority: Int? = null,
    preOrdered: Inclusion? = null,
    wantToPlay: Inclusion? = null,
    wantToBuy: Inclusion? = null,
    previouslyOwned: Inclusion? = null,
    hasParts: Inclusion? = null,
    wantParts: Inclusion? = null,
    minRating: Int? = null,
    rating: Int? = null,
    minBggRating: Int? = null,
    bggRating: Int? = null,
    minimumPlays: Int? = null,
    maxPlays: Int? = null,
    collectionId: Int? = null,
    modifiedSince: LocalDateTime? = null,
) = request {
    client
        .get(BASE_URL) {
            url {
                appendPathSegments(PATH_COLLECTION)
                parameters.apply {
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
                        val formatter = DateTimeFormatter.ofPattern(REQUEST_DATE_TIME_FORMAT)
                        append(PARAM_MODIFIED_SINCE, formatter.format(modifiedSince))
                    }
                }
            }
        }
        .let { mapper.readValue(it.bodyAsText(), Collection::class.java) }
}
