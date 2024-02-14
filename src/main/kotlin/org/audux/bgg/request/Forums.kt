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
import org.audux.bgg.response.Forum
import org.audux.bgg.response.Response

/**
 * Retrieves the list of threads for the given forum id.
 *
 * Note: Pagination data is not returned in the response but can be calculated by
 * `Math.ceil(numThreads/50)`.
 *
 * @param id The id of the forum.
 * @param page Used to paginate, this is the page that is returned, only 50 threads per page are
 *   returned. Note that page 0 and 1 are the same.
 */
internal fun InternalBggClient.forum(id: Int, page: Int? = null) =
    PaginatedForum(this, page ?: 1) {
        client()
            .get(Constants.XML2_API_URL) {
                url {
                    appendPathSegments(Constants.PATH_FORUM)
                    parameters.append(Constants.PARAM_ID, id.toString())
                    page?.let { parameters.append(Constants.PARAM_PAGE, page.toString()) }
                }
            }
            .let { Response.from<Forum>(it.bodyAsText(), mapper) }
    }
