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
package org.audux.bgg.request

import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendPathSegments
import org.audux.bgg.BggClient
import org.audux.bgg.common.HotListType
import org.audux.bgg.response.HotList

/** Hotness endpoint that retrieve the list of most 50 active items on the site filtered by type. */
suspend fun BggClient.hot(
    /**
     * Single [HotListType] returning only items of the specified type, defaults to
     * [HotListType.BOARD_GAME].
     */
    type: HotListType? = null,
): HotList {
    val response =
        client.get(BggClient.BASE_URL) {
            url {
                appendPathSegments(BggClient.PATH_HOT)
                type?.let { parameters.append(BggClient.PARAM_TYPE, it.param) }
            }
        }

    return mapper.readValue(response.bodyAsText(), HotList::class.java)
}
