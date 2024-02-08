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
import org.audux.bgg.common.ForumListType
import org.audux.bgg.response.Family
import org.audux.bgg.response.ForumList
import org.audux.bgg.response.Thing

/**
 * Retrieves the list of available forums for the given id / type combination. e.g. Retrieve all the
 * available forums for `[id=342942, type=thing]` i.e. Ark nova.
 *
 * @param id The id of either the Family or Thing to retrieve
 * @param type Single [ForumListType] to retrieve, either a [Thing] or [Family]
 */
fun BggClient.forumList(id: Int, type: ForumListType) = request {
    client
        .get(Constants.XML2_API_URL) {
            url {
                appendPathSegments(Constants.PATH_FORUM_LIST)
                parameters.apply {
                    append(Constants.PARAM_ID, id.toString())
                    append(Constants.PARAM_TYPE, type.param)
                }
            }
        }
        .let { mapper.readValue(it.bodyAsText(), ForumList::class.java) }
}
