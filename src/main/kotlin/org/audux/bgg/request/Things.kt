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
import org.audux.bgg.BggRequestException
import org.audux.bgg.InternalBggClient
import org.audux.bgg.common.Constants.PARAM_COMMENTS
import org.audux.bgg.common.Constants.PARAM_ID
import org.audux.bgg.common.Constants.PARAM_MARKETPLACE
import org.audux.bgg.common.Constants.PARAM_PAGE
import org.audux.bgg.common.Constants.PARAM_PAGE_SIZE
import org.audux.bgg.common.Constants.PARAM_RATING_COMMENTS
import org.audux.bgg.common.Constants.PARAM_STATS
import org.audux.bgg.common.Constants.PARAM_TYPE
import org.audux.bgg.common.Constants.PARAM_VERSIONS
import org.audux.bgg.common.Constants.PARAM_VIDEOS
import org.audux.bgg.common.Constants.PATH_THING
import org.audux.bgg.common.Constants.XML2_API_URL
import org.audux.bgg.common.ThingType
import org.audux.bgg.response.Response
import org.audux.bgg.response.Things

/** @see org.audux.bgg.BggClient.things */
internal fun InternalBggClient.things(
    ids: Array<Int>,
    types: Array<ThingType>,
    stats: Boolean,
    versions: Boolean,
    videos: Boolean,
    marketplace: Boolean,
    comments: Boolean,
    ratingComments: Boolean,
    page: Int,
    pageSize: Int?,
) =
    PaginatedThings(
        this,
        ids = ids,
        currentPage = page,
        pageSize = pageSize ?: 100,
        comments = comments,
        ratingComments = ratingComments
    ) {
        if (pageSize != null && !(10..100).contains(pageSize)) {
            throw BggRequestException("pageSize must be between 10 and 100")
        }
        if (comments && ratingComments) {
            throw BggRequestException("comments and ratingsComments can't both be true")
        }

        client()
            .get(XML2_API_URL) {
                url {
                    appendPathSegments(PATH_THING)

                    parameters.apply {
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
                        if (page > 1) append(PARAM_PAGE, page.toString())
                        if (pageSize != null) append(PARAM_PAGE_SIZE, pageSize.toString())
                    }
                }
            }
            .let { Response.from<Things>(it.bodyAsText(), mapper) }
    }
