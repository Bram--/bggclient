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
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.audux.bgg.InstantiableClient
import org.audux.bgg.common.Constants
import org.audux.bgg.common.Constants.PARAM_ID
import org.audux.bgg.common.Constants.PARAM_PAGE
import org.audux.bgg.common.Constants.PARAM_SUBTYPE
import org.audux.bgg.common.Constants.PARAM_TYPE
import org.audux.bgg.common.Constants.PARAM_USERNAME
import org.audux.bgg.common.Constants.PATH_PLAYS
import org.audux.bgg.common.Constants.XML2_API_URL
import org.audux.bgg.common.PlayThingType
import org.audux.bgg.common.SubType
import org.audux.bgg.response.Plays
import org.audux.bgg.response.Response

/** @see org.audux.bgg.BggClient.plays */
internal fun InstantiableClient.plays(
    username: String,
    id: Int?,
    type: PlayThingType?,
    minDate: LocalDate?,
    maxDate: LocalDate?,
    subType: SubType?,
    page: Int?,
) =
    PaginatedPlays(this, id, type, minDate, maxDate, subType) {
        val formatter = DateTimeFormatter.ofPattern(Constants.REQUEST_DATE_FORMAT)
        client()
            .get(XML2_API_URL) {
                url {
                    appendPathSegments(PATH_PLAYS)
                    parameters.append(PARAM_USERNAME, username)
                    id?.let { parameters.append(PARAM_ID, it.toString()) }
                    type?.let { parameters.append(PARAM_TYPE, it.param) }
                    minDate?.let {
                        parameters.append(Constants.PARAM_MINIMUM_DATE, formatter.format(it))
                    }
                    maxDate?.let {
                        parameters.append(Constants.PARAM_MAXIMUM_DATE, formatter.format(it))
                    }
                    subType?.let { parameters.append(PARAM_SUBTYPE, it.param) }
                    page?.let { parameters.append(PARAM_PAGE, it.toString()) }
                }
            }
            .let { Response.from<Plays>(it.bodyAsText(), mapper) }
    }
