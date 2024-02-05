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
import org.audux.bgg.BggClient
import org.audux.bgg.common.Inclusion
import org.audux.bgg.request.Constants.BASE_URL
import org.audux.bgg.request.Constants.PARAM_ID
import org.audux.bgg.request.Constants.PARAM_MEMBERS
import org.audux.bgg.request.Constants.PARAM_PAGE
import org.audux.bgg.request.Constants.PARAM_SORT
import org.audux.bgg.request.Constants.PATH_GUILDS
import org.audux.bgg.response.Guild

/**
 * Retrieve information about the given guild (id) like name, description, members etc.
 *
 * @param id ID of the guild you want to view.
 * @param members Include member roster in the results. Member list is paged and sorted.
 * @param sort Specifies how to sort the members list; default is username.
 * @param page The page of the members list to return. Pagesize is 25.
 */
fun BggClient.guilds(id: Number, members: Inclusion?, sort: String?, page: Number?) = request {
    client
        .get(BASE_URL) {
            url {
                appendPathSegments(PATH_GUILDS)
                parameters.append(PARAM_ID, id.toString())
                members?.let { parameters.append(PARAM_MEMBERS, it.toParam()) }
                sort?.let { parameters.append(PARAM_SORT, it) }
                parameters.append(PARAM_PAGE, page.toString())
            }
        }
        .let { mapper.readValue(it.bodyAsText(), Guild::class.java) }
}
