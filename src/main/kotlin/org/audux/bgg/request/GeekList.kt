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
import org.audux.bgg.common.Constants.PARAM_COMMENTS
import org.audux.bgg.common.Constants.PATH_GEEK_LIST
import org.audux.bgg.common.Constants.XML1_API_URL
import org.audux.bgg.common.Inclusion
import org.audux.bgg.response.GeekList
import org.audux.bgg.response.Response

/** @see org.audux.bgg.BggClient.geekList */
internal fun InternalBggClient.geekList(id: Int, comments: Inclusion?) = request {
    client()
        .get(XML1_API_URL) {
            url {
                appendPathSegments(PATH_GEEK_LIST, id.toString())
                comments?.let { parameters.append(PARAM_COMMENTS, it.toParam()) }
            }
        }
        .let { Response.from<GeekList>(it.bodyAsText(), mapper) }
}
