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
import org.audux.bgg.InstantiableClient
import org.audux.bgg.common.Constants
import org.audux.bgg.response.Forum
import org.audux.bgg.response.Response

/** @see org.audux.bgg.BggClient.forum */
internal fun InstantiableClient.forum(id: Int, page: Int?) =
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
