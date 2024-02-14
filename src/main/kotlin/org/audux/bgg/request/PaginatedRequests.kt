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

import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.ceil
import kotlin.math.min
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.response.Play
import org.audux.bgg.response.Plays
import org.audux.bgg.response.Response

internal abstract class PaginatedRequest<T>(
    private val client: BggClient.InternalBggClient,
    private val request: suspend () -> Response<T>
) : Request<T>(client, request) {

    abstract suspend fun paginate(to: Int = Int.MAX_VALUE): Request<T>
}

internal class PaginatedPlays(
    private val client: BggClient.InternalBggClient,
    private val request: suspend () -> Response<Plays>
) : PaginatedRequest<Plays>(client, request) {

    override suspend fun paginate(to: Int): Request<Plays> {
        return Request(client) {
            val plays = request()
            if (plays.isError()) return@Request plays

            val allPlays = CopyOnWriteArrayList<Play>()

            plays.data?.let { playsData ->
                allPlays.addAllAbsent(playsData.plays)

                // Number of pages to paginate.
                val currentPage = playsData.page.toInt()
                val pages = min(ceil(playsData.total.toDouble() / 100).toInt(), to)

                runBlocking {
                    if (pages > currentPage) {
                        val jobs = mutableListOf<Job>()

                        for (i in currentPage..pages) {
                            jobs.add(
                                launch(
                                    block = {
                                        client
                                            .plays(username = playsData.username, page = i)
                                            .call()
                                            .data
                                            ?.let { allPlays.addAllAbsent(it.plays) }
                                    }
                                )
                            )
                        }

                        jobs.onEach { it.join() }
                    }
                }
            }

            plays
        }
    }
}
