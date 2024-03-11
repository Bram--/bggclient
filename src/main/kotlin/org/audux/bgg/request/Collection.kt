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
import org.audux.bgg.InternalBggClient
import org.audux.bgg.common.Constants.PARAM_BGG_RATING
import org.audux.bgg.common.Constants.PARAM_BRIEF
import org.audux.bgg.common.Constants.PARAM_COLLECTION_ID
import org.audux.bgg.common.Constants.PARAM_COMMENT
import org.audux.bgg.common.Constants.PARAM_EXCLUDE_SUBTYPE
import org.audux.bgg.common.Constants.PARAM_HAS_PARTS
import org.audux.bgg.common.Constants.PARAM_ID
import org.audux.bgg.common.Constants.PARAM_MAX_PLAYS
import org.audux.bgg.common.Constants.PARAM_MINIMUM_BGG_RATING
import org.audux.bgg.common.Constants.PARAM_MINIMUM_PLAYS
import org.audux.bgg.common.Constants.PARAM_MINIMUM_RATING
import org.audux.bgg.common.Constants.PARAM_MODIFIED_SINCE
import org.audux.bgg.common.Constants.PARAM_OWN
import org.audux.bgg.common.Constants.PARAM_PLAYED
import org.audux.bgg.common.Constants.PARAM_PREVIOUSLY_OWNED
import org.audux.bgg.common.Constants.PARAM_PRE_ORDERED
import org.audux.bgg.common.Constants.PARAM_RATED
import org.audux.bgg.common.Constants.PARAM_RATING
import org.audux.bgg.common.Constants.PARAM_STATS
import org.audux.bgg.common.Constants.PARAM_SUBTYPE
import org.audux.bgg.common.Constants.PARAM_TRADE
import org.audux.bgg.common.Constants.PARAM_USERNAME
import org.audux.bgg.common.Constants.PARAM_VERSION
import org.audux.bgg.common.Constants.PARAM_WANT
import org.audux.bgg.common.Constants.PARAM_WANT_PARTS
import org.audux.bgg.common.Constants.PARAM_WANT_TO_BUY
import org.audux.bgg.common.Constants.PARAM_WANT_TO_PLAY
import org.audux.bgg.common.Constants.PARAM_WISHLIST
import org.audux.bgg.common.Constants.PARAM_WISHLIST_PRIORITY
import org.audux.bgg.common.Constants.PATH_COLLECTION
import org.audux.bgg.common.Constants.REQUEST_DATE_TIME_FORMAT
import org.audux.bgg.common.Constants.XML2_API_URL
import org.audux.bgg.common.Inclusion
import org.audux.bgg.common.ThingType
import org.audux.bgg.response.Collection
import org.audux.bgg.response.Response

/** @see org.audux.bgg.BggClient.collection */
internal fun InternalBggClient.collection(
    userName: String,
    subType: ThingType,
    excludeSubType: ThingType?,
    ids: Array<Int>?,
    version: Boolean,
    brief: Boolean,
    stats: Boolean,
    own: Inclusion?,
    rated: Inclusion?,
    played: Inclusion?,
    comment: Inclusion?,
    trade: Inclusion?,
    want: Inclusion?,
    wishlist: Inclusion?,
    wishlistPriority: Int?,
    preOrdered: Inclusion?,
    wantToPlay: Inclusion?,
    wantToBuy: Inclusion?,
    previouslyOwned: Inclusion?,
    hasParts: Inclusion?,
    wantParts: Inclusion?,
    minRating: Int?,
    rating: Int?,
    minBggRating: Int?,
    bggRating: Int?,
    minimumPlays: Int?,
    maxPlays: Int?,
    collectionId: Int?,
    modifiedSince: LocalDateTime?,
) = request {
    client()
        .get(XML2_API_URL) {
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
        .let { Response.from<Collection>(it.bodyAsText(), mapper) }
}
