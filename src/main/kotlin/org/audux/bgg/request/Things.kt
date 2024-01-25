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
import io.ktor.util.StringValues
import org.audux.bgg.BggClient
import org.audux.bgg.BggRequestException
import org.audux.bgg.common.ThingType
import org.audux.bgg.request.Constants.BASE_URL
import org.audux.bgg.request.Constants.PARAM_COMMENTS
import org.audux.bgg.request.Constants.PARAM_ID
import org.audux.bgg.request.Constants.PARAM_MARKETPLACE
import org.audux.bgg.request.Constants.PARAM_PAGE
import org.audux.bgg.request.Constants.PARAM_PAGE_SIZE
import org.audux.bgg.request.Constants.PARAM_RATING_COMMENTS
import org.audux.bgg.request.Constants.PARAM_STATS
import org.audux.bgg.request.Constants.PARAM_TYPE
import org.audux.bgg.request.Constants.PARAM_VERSIONS
import org.audux.bgg.request.Constants.PARAM_VIDEOS
import org.audux.bgg.request.Constants.PATH_THING
import org.audux.bgg.response.Things

/**
 * Request a Thing or list of things. Multiple things can be requested by passing in several IDs. At
 * least one ID is required to make this request. Sending along [types] might result in an empty as
 * the API filters based on the [ThingType].
 *
 * @param ids Specifies the id of the thing(s) to retrieve. To request multiple things with a single
 *   query, can specify a comma-delimited list of ids.
 * @param types Specifies that, regardless of the type of thing asked for by id, the results are
 *   filtered by the [ThingType] objects specified. Leave empty to return all types.
 * @param stats Returns ranking and rating stats for the thing.
 * @param versions Returns version info for the thing.
 * @param videos Returns videos for the thing.
 * @param marketplace Returns marketplace data.
 * @param comments Returns all comments about the thing. Also includes ratings when commented. See
 *   page parameter.
 * @param ratingComments Returns all ratings for the thing. Also includes comments when rated. See
 *   page parameter. The [ratingComments] and [comments] parameters cannot be used together, as the
 *   output always appears in the <comments> node of the XML; comments parameter takes precedence if
 *   both are specified. Ratings are sorted in descending rating value, based on the highest rating
 *   they have assigned to that thing (each thing in the collection can have a different rating).
 * @param page Defaults to 1, controls the page of data to see for comments, and ratings data.
 * @param pageSize Set the number of records to return in paging. Minimum is 10, maximum is 100.
 *   Defaults to 100.
 */
fun BggClient.things(
    ids: Array<Int>,
    types: Array<ThingType> = arrayOf(),
    stats: Boolean = false,
    versions: Boolean = false,
    videos: Boolean = false,
    marketplace: Boolean = false,
    comments: Boolean = false,
    ratingComments: Boolean = false,
    page: Int = 0,
    pageSize: Int = 0,
): Request<Things> {
    return request {
        if (pageSize != 0 && !(10..100).contains(pageSize)) {
            throw BggRequestException("pageSize must be between 10 and 100")
        }
        if (comments && ratingComments) {
            throw BggRequestException("comments and ratingsComments can't both be true")
        }

        val response =
            client.get(BASE_URL) {
                url {
                    appendPathSegments(PATH_THING)

                    parameters.appendAll(
                        StringValues.build {
                            append(PARAM_ID, ids.joinToString(","))

                            if (types.isNotEmpty()) {
                                append(PARAM_TYPE, types.joinToString(",") { it.param })
                            }

                            if (stats) append(PARAM_STATS, "1")
                            if (versions) append(PARAM_VERSIONS, "1")
                            if (videos) append(PARAM_VIDEOS, "1")
                            if (marketplace) append(PARAM_MARKETPLACE, "1")
                            if (comments) append(PARAM_COMMENTS, "1")
                            if (ratingComments) append(PARAM_RATING_COMMENTS, "1")
                            if (page > 0) append(PARAM_PAGE, page.toString())
                            if (pageSize > 0) append(PARAM_PAGE_SIZE, pageSize.toString())
                        }
                    )
                }
            }

        mapper.readValue(response.bodyAsText(), Things::class.java)
    }
}
