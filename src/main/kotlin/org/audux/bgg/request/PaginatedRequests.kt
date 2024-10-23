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
import java.time.LocalDate
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.jvm.Throws
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.BggRequestException
import org.audux.bgg.InternalBggClient
import org.audux.bgg.common.Domain
import org.audux.bgg.common.Inclusion
import org.audux.bgg.common.PlayThingType
import org.audux.bgg.common.SubType
import org.audux.bgg.response.Buddy
import org.audux.bgg.response.Forum
import org.audux.bgg.response.Guild
import org.audux.bgg.response.GuildMember
import org.audux.bgg.response.GuildReference
import org.audux.bgg.response.Play
import org.audux.bgg.response.Plays
import org.audux.bgg.response.Response
import org.audux.bgg.response.Thing
import org.audux.bgg.response.Things
import org.audux.bgg.response.ThreadSummary
import org.audux.bgg.response.User

/**
 * Allows pagination on compatible API requests. Using [paginate] will automatically request all
 * pages from page of the initial request. By default it paginates all pages but this can be
 * controlled by the [to] parameter.
 *
 * @param client The [BggClient] that's running this request
 * @param request The initial request that should be paginated, it will paginate FROM the first page
 *   requested i.e. if the [request] makes a request with `page = 10` pagination will only happen
 *   from 10 onwards.
 */
abstract class PaginatedRequest<T>
internal constructor(client: InternalBggClient, request: suspend () -> Response<T>) :
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
     *
     * @param toPage Paginate from the initial request to the `toPage`
     * @throws BggRequestException Thrown when something went wrong in the initial request
     */
    @Throws(BggRequestException::class) abstract fun paginate(toPage: Int): Request<T>

    /**
     * No-arg implementation for Java.
     *
     * @see paginate
     * @suppress
     */
    @Throws(BggRequestException::class) fun paginate() = paginate(Int.MAX_VALUE)
}

/** [PaginatedRequest] implementation for [forum]. */
class PaginatedForum
internal constructor(
    private val client: InternalBggClient,
    private val currentPage: Int,
    private val request: suspend () -> Response<Forum>,
) : PaginatedRequest<Forum>(client, request) {
    /** @suppress */
    companion object {
        /**
         * The pageSize for forum requests i.e. the number of [ThreadSummary]s returned per request.
         */
        const val PAGE_SIZE = 50
    }

    override fun paginate(toPage: Int) =
        Request(client) {
            // Run the initial request
            request().let { forum ->
                if (forum.data == null) return@Request forum
                val allThreads =
                    CopyOnWriteArrayList<ThreadSummary>().apply { addAllAbsent(forum.data.threads) }
                val lastPage =
                    min(toPage, ceil(forum.data.numThreads.toDouble() / PAGE_SIZE).toInt())

                // Start pagination concurrently.
                concurrentRequests((currentPage + 1)..lastPage) { page ->
                    val response = client.forum(id = forum.data.id, page = page).call()

                    if (response.isError()) {
                        // Ignore errors but do log them.
                        Logger.w("Error paginating forum page $page")
                    } else {
                        // Collect all Threads
                        response.data?.let { allThreads.addAllAbsent(it.threads) }
                    }
                }

                // Finally copy all the collected threads into the initial Forum object.
                forum.copy(data = forum.data.copy(threads = allThreads.toList()))
            }
        }
}

/** [PaginatedRequest] implementation for [guild]. */
class PaginatedGuilds
internal constructor(
    private val client: InternalBggClient,
    private val members: Inclusion?,
    private val sort: String?,
    private val request: suspend () -> Response<Guild>,
) : PaginatedRequest<Guild>(client, request) {
    /** @suppress */
    companion object {
        /**
         * The pageSize for guild requests i.e. the number of [GuildMember]s returned per request.
         */
        const val PAGE_SIZE = 25
    }

    override fun paginate(toPage: Int) =
        Request(client) {
            if (members != Inclusion.INCLUDE) {
                throw BggRequestException("Nothing to paginate without the members parameter set")
            }

            // Run the initial request
            request().let { guild ->
                if (guild.data == null) return@Request guild
                val allGuildMembers = CopyOnWriteArrayList<GuildMember>()
                var lastPage = 1

                // Only if there are actually members returned does it make sense to paginate.
                guild.data.members?.let { guildMembers ->
                    allGuildMembers.addAllAbsent(guildMembers.members)

                    // Int of pages to paginate: (CurrentPage + 1)..lastPage.
                    val currentPage = guildMembers.page
                    lastPage = min(ceil(guildMembers.count.toDouble() / PAGE_SIZE).toInt(), toPage)

                    // Start pagination concurrently.
                    concurrentRequests((currentPage + 1)..lastPage) { page ->
                        val response =
                            client
                                .guild(
                                    id = guild.data.id,
                                    page = page,
                                    members = members,
                                    sort = sort,
                                )
                                .call()

                        if (response.isError()) {
                            // Ignore errors but do log them.
                            Logger.w("Error paginating guilds page $page")
                        } else {
                            // Collect all GuildMembers
                            response.data?.let { allGuildMembers.addAllAbsent(it.members?.members) }
                        }
                    }
                }

                // Finally copy all the collected GuildMembers into the initial Guild object.
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

/** [PaginatedRequest] implementation for [plays]. */
class PaginatedPlays
internal constructor(
    private val client: InternalBggClient,
    private val id: Int?,
    private val type: PlayThingType?,
    private val minDate: LocalDate?,
    private val maxDate: LocalDate?,
    private val subType: SubType?,
    private val request: suspend () -> Response<Plays>,
) : PaginatedRequest<Plays>(client, request) {
    /** @suppress */
    companion object {
        /** The pageSize for plays requests i.e. the number of [Play]s returned per request. */
        const val PAGE_SIZE = 100
    }

    override fun paginate(toPage: Int) =
        Request(client) {
            // Run the initial request
            request().let { plays ->
                if (plays.data == null) return@Request plays
                val allPlays = CopyOnWriteArrayList<Play>().apply { addAllAbsent(plays.data.plays) }

                // Int of pages to paginate: (CurrentPage + 1)..lastPage.
                val currentPage = plays.data.page
                val lastPage = min(ceil(plays.data.total.toDouble() / PAGE_SIZE).toInt(), toPage)

                // Start pagination concurrently.
                concurrentRequests((currentPage + 1)..lastPage) { page ->
                    val response =
                        client
                            .plays(
                                username = plays.data.username,
                                id = id,
                                page = page,
                                type = type,
                                minDate = minDate,
                                maxDate = maxDate,
                                subType = subType,
                            )
                            .call()
                    if (response.isError()) {
                        // Ignore errors but do log them.
                        Logger.w("Error paginating plays page $page")
                    } else {
                        // Collect all Plays
                        response.data?.let { allPlays.addAllAbsent(it.plays) }
                    }
                }

                // Finally copy all the collected plays into the initial Plays object.
                plays.copy(data = plays.data.copy(page = lastPage, plays = allPlays.toList()))
            }
        }
}

/** [PaginatedRequest] implementation for [things]. */
class PaginatedThings
internal constructor(
    private val client: InternalBggClient,
    private val ids: Array<Int>,
    private val currentPage: Int,
    private val pageSize: Int,
    private val comments: Boolean,
    private val ratingComments: Boolean,
    private val request: suspend () -> Response<Things>,
) : PaginatedRequest<Things>(client, request) {
    override fun paginate(toPage: Int) =
        Request(client) {
            if (!comments && !ratingComments) {
                throw BggRequestException(
                    "Nothing to paginate without either the comments or ratingComments parameter set."
                )
            }

            // Run the initial request
            request().let { things ->
                if (things.data == null) return@Request things
                // Create a Concurrent Hashmap to collect Things and update their comments.
                val thingList =
                    ConcurrentHashMap<Int, Thing>().apply {
                        putAll(mapOf(*things.data.things.map { Pair(it.id, it) }.toTypedArray()))
                    }

                // Int of pages to paginate: (CurrentPage + 1)..lastPage.
                val maxComments =
                    things.data.things.maxOfOrNull { it.comments?.totalItems ?: 0 } ?: 0
                val lastPage = min(toPage, ceil(maxComments.toDouble() / pageSize).toInt())

                // Start pagination concurrently.
                concurrentRequests((currentPage + 1)..lastPage) { page ->
                    val response =
                        BggClient.things(
                                ids = ids,
                                page = page,
                                pageSize = pageSize,
                                comments = comments,
                                ratingComments = ratingComments,
                            )
                            .call()

                    if (response.isError()) {
                        // Ignore errors but do log them.
                        Logger.w("Error paginating things page $page")
                    } else {
                        // Collect all Things' comments.
                        response.data?.let {
                            it.things.forEach { newThing ->
                                val existingThing = thingList.getValue(newThing.id)
                                if (newThing.comments == null) return@forEach
                                if (existingThing.comments == null) return@forEach

                                // Update the Thing by adding comments to the existing Thing.
                                thingList[newThing.id] =
                                    existingThing.copy(
                                        comments =
                                            existingThing.comments.copy(
                                                page = lastPage,
                                                comments =
                                                    existingThing.comments.comments +
                                                        newThing.comments.comments,
                                            )
                                    )
                            }
                        }
                    }
                }

                // Finally copy all the collected Things into the initial Things object.
                things.copy(data = things.data.copy(things = thingList.values.toList()))
            }
        }
}

/**
 * [PaginatedRequest] implementation for [User]. Paginates [User.buddies] and/or [User.guilds]
 * depending on which are available in the response.
 */
class PaginatedUser
internal constructor(
    private val client: InternalBggClient,
    private val buddies: Inclusion?,
    private val guilds: Inclusion?,
    private val top: Inclusion?,
    private val hot: Inclusion?,
    private val domain: Domain?,
    private val request: suspend () -> Response<User>,
) : PaginatedRequest<User>(client, request) {
    /** @suppress */
    companion object {
        /**
         * The pageSize for user requests i.e. the number of [Buddy]s and [GuildReference]s returned
         * per request.
         */
        const val PAGE_SIZE = 1_000
    }

    override fun paginate(toPage: Int) =
        Request(client) {
            if (buddies != Inclusion.INCLUDE && guilds != Inclusion.INCLUDE) {
                throw BggRequestException(
                    "Nothing to paginate without either the buddies or guilds parameter set."
                )
            }
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

                // Int of pages to paginate.
                val currentPage = user.data.guilds?.page ?: user.data.buddies?.page ?: 1

                // Calculate the last page needed to request.
                val maxPage = max(user.data.guilds?.total ?: 1, user.data.buddies?.total ?: 1)
                val lastPage = min(ceil(maxPage.toDouble() / PAGE_SIZE).toInt(), toPage)

                // Retrieve all pages
                concurrentRequests((currentPage + 1)..lastPage) { page ->
                    val response =
                        client
                            .user(
                                name = user.data.name,
                                guilds = guilds,
                                buddies = buddies,
                                top = top,
                                hot = hot,
                                page = page,
                                domain = domain,
                            )
                            .call()

                    if (response.isError()) {
                        // Ignore errors but do log them.
                        Logger.w("Error paginating user page $page")
                    } else {
                        // Collect all user's guilds and buddies.
                        response.data?.let { paginatedUser ->
                            paginatedUser.guilds?.let { allGuilds.addAllAbsent(it.guilds) }
                            paginatedUser.buddies?.let { allBuddies.addAllAbsent(it.buddies) }
                        }
                    }
                }

                // Finally copy all the collected Buddies and Guilds into the initial User object.
                user.copy(
                    data =
                        user.data.copy(
                            buddies =
                                user.data.buddies?.copy(
                                    page = lastPage,
                                    buddies = allBuddies.toList(),
                                ),
                            guilds =
                                user.data.guilds?.copy(page = lastPage, guilds = allGuilds.toList()),
                        )
                )
            }
        }
}

/**
 * Runs `pages.first`..`pages.last` pagination requests, where the actual request happens inside
 * [request].
 */
internal suspend inline fun <T> concurrentRequests(
    pages: IntRange,
    crossinline request: suspend (page: Int) -> T,
) {
    val jobs = CopyOnWriteArrayList<Job>()
    runBlocking {
        pages.forEach { jobs.add(launch { request(it) }) }

        // Wait for all requests to complete.
        jobs.forEach { it.join() }
    }
}
