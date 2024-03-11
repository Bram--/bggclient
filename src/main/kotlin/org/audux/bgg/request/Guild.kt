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
import org.audux.bgg.InternalBggClient
import org.audux.bgg.common.Constants.PARAM_ID
import org.audux.bgg.common.Constants.PARAM_MEMBERS
import org.audux.bgg.common.Constants.PARAM_PAGE
import org.audux.bgg.common.Constants.PARAM_SORT
import org.audux.bgg.common.Constants.PATH_GUILDS
import org.audux.bgg.common.Constants.XML2_API_URL
import org.audux.bgg.common.Inclusion
import org.audux.bgg.response.Guild
import org.audux.bgg.response.Response

/** @see org.audux.bgg.BggClient.guild */
internal fun InternalBggClient.guild(
    id: Int,
    members: Inclusion?,
    sort: String?,
    page: Int?,
) =
    PaginatedGuilds(this, members, sort) {
        client()
            .get(XML2_API_URL) {
                url {
                    appendPathSegments(PATH_GUILDS)
                    parameters.append(PARAM_ID, id.toString())
                    members?.let { parameters.append(PARAM_MEMBERS, it.toParam()) }
                    sort?.let { parameters.append(PARAM_SORT, it) }
                    page?.let { parameters.append(PARAM_PAGE, it.toString()) }
                }
            }
            .let { Response.from<Guild>(it.bodyAsText(), mapper) }
    }
