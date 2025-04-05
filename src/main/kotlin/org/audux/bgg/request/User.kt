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
import org.audux.bgg.common.Constants.PARAM_BUDDIES
import org.audux.bgg.common.Constants.PARAM_DOMAIN
import org.audux.bgg.common.Constants.PARAM_GUILDS
import org.audux.bgg.common.Constants.PARAM_HOT
import org.audux.bgg.common.Constants.PARAM_NAME
import org.audux.bgg.common.Constants.PARAM_PAGE
import org.audux.bgg.common.Constants.PARAM_TOP
import org.audux.bgg.common.Constants.PATH_USER
import org.audux.bgg.common.Constants.XML2_API_URL
import org.audux.bgg.common.Domain
import org.audux.bgg.common.Inclusion
import org.audux.bgg.response.Response
import org.audux.bgg.response.User

/** @see org.audux.bgg.BggClient.user */
fun InstantiableClient.user(
    name: String,
    buddies: Inclusion?,
    guilds: Inclusion?,
    top: Inclusion?,
    hot: Inclusion?,
    domain: Domain?,
    page: Int?,
) =
    PaginatedUser(this, buddies, guilds, top, hot, domain) {
        client()
            .get(XML2_API_URL) {
                url {
                    appendPathSegments(PATH_USER)
                    parameters.append(PARAM_NAME, name)
                    buddies?.let { parameters.append(PARAM_BUDDIES, it.toParam()) }
                    guilds?.let { parameters.append(PARAM_GUILDS, it.toParam()) }
                    top?.let { parameters.append(PARAM_TOP, it.toParam()) }
                    hot?.let { parameters.append(PARAM_HOT, it.toParam()) }
                    domain?.let { parameters.append(PARAM_DOMAIN, it.param) }
                    page?.let { parameters.append(PARAM_PAGE, it.toString()) }
                }
            }
            .let { Response.from<User>(it.bodyAsText(), mapper) }
    }
