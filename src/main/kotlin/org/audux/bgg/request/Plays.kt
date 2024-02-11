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
import org.audux.bgg.BggClient
import org.audux.bgg.common.PlayThingType
import org.audux.bgg.common.SubType
import org.audux.bgg.request.Constants.PARAM_ID
import org.audux.bgg.request.Constants.PARAM_PAGE
import org.audux.bgg.request.Constants.PARAM_SUBTYPE
import org.audux.bgg.request.Constants.PARAM_TYPE
import org.audux.bgg.request.Constants.PARAM_USERNAME
import org.audux.bgg.request.Constants.PATH_PLAYS
import org.audux.bgg.request.Constants.XML2_API_URL
import org.audux.bgg.response.Plays
import org.audux.bgg.response.Response

/**
 * Request a list of plays (max 100 at the time) for the given user.
 *
 * @param username Name of the player you want to request play information for. Data is returned in
 *   backwards-chronological form. You must include either a username or an id and type to get
 *   results.
 * @param id Id number of the item you want to request play information for. Data is returned in
 *   backwards-chronological form.
 * @param type Type of the item you want to request play information for. Valid types include: thing
 *   family
 * @param minDate Returns only plays of the specified date or later.
 * @param maxDate Returns only plays of the specified date or earlier.
 * @param subType=TYPE Limits play results to the specified TYPE; boardgame is the default.
 * @param page The page of information to request. Page size is 100 records.
 */
fun BggClient.plays(
    username: String,
    id: Number? = null,
    type: PlayThingType? = null,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null,
    subType: SubType? = null,
    page: Number? = null,
) = request {
    val formatter = DateTimeFormatter.ofPattern(Constants.REQUEST_DATE_FORMAT)
    client
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
