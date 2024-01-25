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
import org.audux.bgg.common.FamilyType
import org.audux.bgg.common.HotListType
import org.audux.bgg.response.Family

/**
 * Family thing endpoint that retrieve details about the given family ID and associated `Link`
 * objects.
 *
 * @param ids array of IDs returning only families of the specified id.
 * @param types Single [HotListType] returning only items of the specified type, defaults to
 *   [HotListType.BOARD_GAME].
 */
fun BggClient.family(ids: Array<Int>, types: Array<FamilyType> = arrayOf()): Request<Family> =
    request {
        client
            .get(Constants.BASE_URL) {
                url {
                    appendPathSegments(Constants.PATH_FAMILY)
                    parameters.apply {
                        append(Constants.PARAM_ID, ids.joinToString(","))
                        if (types.isNotEmpty()) {
                            append(Constants.PARAM_TYPE, types.joinToString(",") { it.param })
                        }
                    }
                }
            }
            .let { mapper.readValue(it.bodyAsText(), Family::class.java) }
    }
