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

import co.touchlab.kermit.Logger
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.common.Inclusion
import org.audux.bgg.response.Buddy
import org.audux.bgg.response.Forum
import org.audux.bgg.response.Guild
import org.audux.bgg.response.GuildMember
import org.audux.bgg.response.GuildReference
import org.audux.bgg.response.Play
import org.audux.bgg.response.Plays
import org.audux.bgg.response.Response
import org.audux.bgg.response.Things
import org.audux.bgg.response.ThreadSummary
import org.audux.bgg.response.User

/**
 * Allows pagination on compatible API requests. Using [paginate] will automatically request all
 * pages from page of the initial request. By default it paginates all pages but this can be
 * controlled by the [to] parameter.
 */
abstract class PaginatedRequest<T>
internal constructor(client: BggClient.InternalBggClient, request: suspend () -> Response<T>) :
    Request<T>(client, request) {

    /**
     * Paginate all pages or only up to [toPage], if set. If [toPage] is higher than the actual
     * available pages no additional requests are made and instead will stop paginating after the
     * final page was requests.
     *
     * The returned [T] will have `page` set to the last page that was requested and [T] data class
     * will have the paginated items copied in. i.e. when requesting 'plays' fpr a user with 300
     * plays the resulting [Plays] object will look something as follows:
     * ```
     * Plays(total = 3, ...., page = 3, plays = List<Play>( /* 300 items in here */)
     * ```
     */
    abstract suspend fun paginate(toPage: Int = Int.MAX_VALUE): Request<T>
}

/** Pagination implementation for [forum]. */
class PaginatedForum
internal constructor(
    private val client: BggClient.InternalBggClient,
    private val currentPage: Int,
    private val request: suspend () -> Response<Forum>
) : PaginatedRequest<Forum>(client, request) {
    override suspend fun paginate(toPage: Int) =
        Request(client) {
            request().let { forum ->
                if (forum.data == null) return@Request forum
                val allThreads =
                    CopyOnWriteArrayList<ThreadSummary>().apply { addAllAbsent(forum.data.threads) }
                val lastPage = ceil(forum.data.numThreads.toDouble() / 50).toInt()

                concurrentRequests((currentPage + 1)..lastPage) { page ->
                    val response = client.forum(id = forum.data.id, page = page).call()

                    if (response.isError()) {
                        Logger.w("Error paginating guilds page $page")
                    } else {
                        response.data?.let { allThreads.addAllAbsent(it.threads) }
                    }
                }

                forum.copy(data = forum.data.copy(threads = allThreads.toList()))
            }
        }
}

/** Pagination implementation for [guilds]. */
class PaginatedGuilds
internal constructor(
    private val client: BggClient.InternalBggClient,
    private val members: Inclusion? = null,
    private val request: suspend () -> Response<Guild>
) : PaginatedRequest<Guild>(client, request) {

    override suspend fun paginate(toPage: Int) =
        Request(client) {
            request().let { guild ->
                if (guild.data == null) return@Request guild
                val allGuildMembers = CopyOnWriteArrayList<GuildMember>()
                var lastPage = 1

                guild.data.members?.let { guildMembers ->
                    allGuildMembers.addAllAbsent(guildMembers.members)

                    // Number of pages to paginate.
                    val currentPage = guildMembers.page.toInt()
                    lastPage = min(ceil(guildMembers.count.toDouble() / 25).toInt(), toPage)

                    concurrentRequests((currentPage + 1)..lastPage) { page ->
                        val response =
                            client.guilds(id = guild.data.id, page = page, members = members).call()

                        if (response.isError()) {
                            Logger.w("Error paginating guilds page $page")
                        } else {
                            response.data?.let { allGuildMembers.addAllAbsent(it.members?.members) }
                        }
                    }
                }

                guild.copy(
                    data =
                        guild.data.copy(
                            members =
                                guild.data.members?.copy(members = allGuildMembers, page = lastPage)
                        )
                )
            }
        }
}

/** Pagination implementation for [Plays]. */
class PaginatedPlays
internal constructor(
    private val client: BggClient.InternalBggClient,
    private val request: suspend () -> Response<Plays>
) : PaginatedRequest<Plays>(client, request) {
    override suspend fun paginate(toPage: Int) =
        Request(client) {
            request().let { plays ->
                if (plays.data == null) return@Request plays
                val allPlays = CopyOnWriteArrayList<Play>().apply { addAllAbsent(plays.data.plays) }

                // Number of pages to paginate.
                val currentPage = plays.data.page.toInt()
                val lastPage = min(ceil(plays.data.total.toDouble() / 100).toInt(), toPage)

                concurrentRequests((currentPage + 1)..lastPage) { page ->
                    val response = client.plays(username = plays.data.username, page = page).call()
                    if (response.isError()) {
                        Logger.w("Error paginating plays page $page")
                    } else {
                        response.data?.let { allPlays.addAllAbsent(it.plays) }
                    }
                }

                plays.copy(data = plays.data.copy(page = lastPage, plays = allPlays.toList()))
            }
        }
}

/** Pagination implementation for [forum]. */
class PaginatedThings
internal constructor(
    private val client: BggClient.InternalBggClient,
    private val ids: Array<Int>,
    private val currentPage: Int,
    private val pageSize: Int,
    private val comments: Boolean,
    private val ratingComments: Boolean,
    private val request: suspend () -> Response<Things>
) : PaginatedRequest<Things>(client, request) {
    override suspend fun paginate(toPage: Int) =
        Request(client) {
            request().let { things ->
                if (things.data == null) return@Request things

                val maxComments =
                    things.data.things.maxOfOrNull { it.comments?.totalItems ?: 0 } ?: 0
                val lastPage = ceil(maxComments.toDouble() / pageSize).toInt()
                val thingList =
                    mutableMapOf(*things.data.things.map { Pair(it.id, it) }.toTypedArray())

                concurrentRequests((currentPage + 1)..lastPage) { page ->
                    val response =
                        BggClient.things(
                                ids = ids,
                                page = page,
                                pageSize = pageSize,
                                comments = comments,
                                ratingComments = ratingComments
                            )
                            .call()

                    if (response.isError()) {
                        Logger.w("Error paginating guilds page $page")
                    } else {
                        response.data?.let {
                            it.things.forEach { newThing ->
                                val existingThing = thingList.getValue(newThing.id)
                                if (newThing.comments == null) return@forEach
                                if (existingThing.comments == null) return@forEach

                                thingList[newThing.id] =
                                    existingThing.copy(
                                        comments =
                                            existingThing.comments.copy(
                                                page = lastPage,
                                                comments =
                                                    existingThing.comments.comments +
                                                        newThing.comments.comments
                                            )
                                    )
                            }
                        }
                    }
                }

                things.copy(data = things.data.copy(things = thingList.values.toList()))
            }
        }
}

/**
 * Pagination implementation for [User]. Paginates [User.buddies] and/or [User.guilds] depending on
 * which are available in the response.
 */
class PaginatedUser
internal constructor(
    private val client: BggClient.InternalBggClient,
    private val buddies: Inclusion?,
    private val guilds: Inclusion?,
    private val request: suspend () -> Response<User>,
) : PaginatedRequest<User>(client, request) {
    override suspend fun paginate(toPage: Int) =
        Request(client) {
            request().let { user ->
                if (user.data == null) return@Request user
                if (user.data.buddies == null && user.data.guilds == null) return@Request user
                val allGuilds =
                    CopyOnWriteArrayList<GuildReference>().apply {
                        user.data.guilds?.let { addAllAbsent(it.guilds) }
                    }
                val allBuddies =
                    CopyOnWriteArrayList<Buddy>().apply {
                        user.data.buddies?.let { addAllAbsent(it.buddies) }
                    }

                // Number of pages to paginate.
                val currentPage =
                    user.data.guilds?.page?.toInt() ?: user.data.buddies?.page?.toInt() ?: 1

                // Calculate the last page needed to request.
                val maxPage =
                    max(
                        user.data.guilds?.total?.toInt() ?: 1,
                        user.data.buddies?.total?.toInt() ?: 1
                    )
                val lastPage = min(ceil(maxPage.toDouble() / 1_000).toInt(), toPage)

                // Retrieve all pages
                concurrentRequests((currentPage + 1)..lastPage) { page ->
                    val response =
                        client
                            .user(
                                name = user.data.name,
                                guilds = guilds,
                                buddies = buddies,
                                page = page,
                            )
                            .call()

                    if (response.isError()) {
                        Logger.w("Error paginating user page $page")
                    } else {
                        response.data?.let { paginatedUser ->
                            paginatedUser.guilds?.let { allGuilds.addAllAbsent(it.guilds) }
                            paginatedUser.buddies?.let { allBuddies.addAllAbsent(it.buddies) }
                        }
                    }
                }

                user.copy(
                    data =
                        user.data.copy(
                            buddies =
                                user.data.buddies?.copy(
                                    page = lastPage,
                                    buddies = allBuddies.toList()
                                ),
                            guilds =
                                user.data.guilds?.copy(
                                    page = lastPage,
                                    guilds = allGuilds.toList()
                                ),
                        )
                )
            }
        }
}

private suspend inline fun <T> concurrentRequests(
    pages: IntRange,
    crossinline request: suspend (page: Int) -> T
) {
    val jobs = mutableListOf<Job>()
    runBlocking {
        pages.forEach {
            jobs.add(launch { request(it) })

            jobs.onEach { it.join() }
        }
    }
}
