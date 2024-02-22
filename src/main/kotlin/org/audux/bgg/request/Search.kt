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
import org.audux.bgg.BggClient.InternalBggClient
import org.audux.bgg.common.Constants.PARAM_EXACT
import org.audux.bgg.common.Constants.PARAM_QUERY
import org.audux.bgg.common.Constants.PARAM_TYPE
import org.audux.bgg.common.Constants.PATH_SEARCH
import org.audux.bgg.common.Constants.XML2_API_URL
import org.audux.bgg.common.ThingType
import org.audux.bgg.response.Response
import org.audux.bgg.response.SearchResults

/**
 * Search endpoint that allows searching by name for things on BGG.
 *
 * @param query Returns all types of items that match [query]. Spaces in the SEARCH_QUERY are
 *   replaced by a
 * @param types Returns all items that match SEARCH_QUERY of type [ThingType]. You can return
 *   multiple types by using more.
 * @param exactMatch Limit results to items that match the [query] exactly
 */
internal fun InternalBggClient.search(
    query: String,
    types: Array<ThingType> = arrayOf(),
    exactMatch: Boolean,
) = request {
    client()
        .get(XML2_API_URL) {
            url {
                appendPathSegments(PATH_SEARCH)
                parameters.apply {
                    append(PARAM_QUERY, query)
                    if (types.isNotEmpty()) {
                        append(PARAM_TYPE, types.joinToString(",") { it.param })
                    }
                    if (exactMatch) append(PARAM_EXACT, "1")
                }
            }
        }
        .let { Response.from<SearchResults>(it.bodyAsText(), mapper) }
}
