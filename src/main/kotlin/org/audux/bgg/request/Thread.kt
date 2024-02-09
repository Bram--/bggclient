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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import org.audux.bgg.BggClient
import org.audux.bgg.response.Thread

/**
 * Retrieves the list of articles/posts for the given thread.
 *
 * @param id The id of the thread.
 * @param minArticleId Filters the results so that only articles with an equal or higher id than NNN
 *   will be returned.
 * @param minArticleDate Filters the results so that only articles after the specified date an time
 *   (HH:MM:SS) or later will be returned.
 * @param count Limits the number of articles returned to no more than NNN.
 */
fun BggClient.thread(
    id: Int,
    minArticleId: Int? = null,
    minArticleDate: LocalDateTime? = null,
    count: Int? = null
) = request {
    client
        .get(Constants.XML2_API_URL) {
            url {
                appendPathSegments(Constants.PATH_THREAD)
                parameters.apply {
                    append(Constants.PARAM_ID, id.toString())
                    minArticleId?.let { append(Constants.PARAM_MINIMUM_ARTICLE_ID, it.toString()) }
                    minArticleDate?.let {
                        val formatter =
                            DateTimeFormatter.ofPattern(Constants.REQUEST_DATE_TIME_FORMAT)
                        append(Constants.PARAM_MINIMUM_ARTICLE_DATE, formatter.format(it))
                    }
                    count?.let { append(Constants.PARAM_COUNT, count.toString()) }
                }
            }
        }
        .let { mapper.readValue(it.bodyAsText(), Thread::class.java) }
}
