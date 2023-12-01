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
import org.audux.bgg.data.common.ThingType
import org.audux.bgg.data.response.SearchResults

/** Search endpoint that allows searching by name for things on BGG. */
suspend fun BggClient.search(
    /**
     * Returns all types of items that match [query]. Spaces in the SEARCH_QUERY are replaced by a +
     */
    query: String,

    /**
     * Return all items that match SEARCH_QUERY of type [ThingType]. You can return multiple types
     * by using more.
     */
    types: Array<ThingType> = arrayOf(),

    /** Limit results to items that match the [query] exactly */
    exactMatch: Boolean = false,
): SearchResults {
    val response =
        client.get(BggClient.BASE_URL) {
            url {
                appendPathSegments(BggClient.PATH_SEARCH)
                parameters.appendAll(
                    StringValues.build {
                        append(BggClient.PARAM_QUERY, query.replace(" ", "+"))
                        if (exactMatch) append(BggClient.PARAM_EXACT, "1")

                        if (types.isNotEmpty()) {
                            append(BggClient.PARAM_TYPE, types.joinToString { "${it.param}," })
                        }
                    }
                )
            }
        }

    return mapper.readValue(response.bodyAsText(), SearchResults::class.java)
}
