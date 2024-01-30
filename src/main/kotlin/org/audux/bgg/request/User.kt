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
import org.audux.bgg.request.Constants.BASE_URL
import org.audux.bgg.request.Constants.PARAM_TYPE
import org.audux.bgg.request.Constants.PATH_HOT
import org.audux.bgg.response.HotList

/**
 * Hotness endpoint that retrieve the list of most 50 active items on the site filtered by type.
 *
 * @param name Specifies the user name (only one user is request-able at a time).
 * @param buddies Turns on buddies reporting. Results are paged; see page parameter.
 * @param guilds Turns on optional guilds reporting. Results are paged; see page parameter.
 * @param hot Include the user's hot 10 list from their profile. Omitted if empty.
 * @param top Include the user's top 10 list from their profile. Omitted if empty.
 * @param domain Controls the domain for the users hot 10 and top 10 lists. The DOMAIN default is
 *   boardgame; valid values are: boardgame, rpg, or videogame
 * @param page Specifies the page of buddy and guild results to return. The default page is 1 if you
 *   don't specify it; page size is 100 records (Current implementation seems to return 1000
 *   records). The page parameter controls paging for both buddies and guilds list if both are
 *   specified. If a <buddies> or <guilds> node is empty, it means that you have requested a page
 *   higher than that needed to list all the buddies/guilds or, if you're on page 1, it means that
 *   that user has no buddies and is not part of any guilds.
 */
fun BggClient.user(name: String) = request {
    client
        .get(BASE_URL) {
            url {
                appendPathSegments(PATH_HOT)
                parameters.append(PARAM_TYPE, name)
            }
        }
        .let { mapper.readValue(it.bodyAsText(), HotList::class.java) }
}
