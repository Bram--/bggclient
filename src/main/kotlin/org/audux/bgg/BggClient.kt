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
package org.audux.bgg

import co.touchlab.kermit.Logger
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpRequestRetry
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.future
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.audux.bgg.common.Domains
import org.audux.bgg.common.FamilyType
import org.audux.bgg.common.ForumListType
import org.audux.bgg.common.HotListType
import org.audux.bgg.common.Inclusion
import org.audux.bgg.common.PlayThingType
import org.audux.bgg.common.SubType
import org.audux.bgg.common.ThingType
import org.audux.bgg.plugin.ClientRateLimitPlugin
import org.audux.bgg.request.Request
import org.audux.bgg.request.collection
import org.audux.bgg.request.familyItems
import org.audux.bgg.request.forum
import org.audux.bgg.request.forumList
import org.audux.bgg.request.geekList
import org.audux.bgg.request.guilds
import org.audux.bgg.request.hotItems
import org.audux.bgg.request.plays
import org.audux.bgg.request.search
import org.audux.bgg.request.things
import org.audux.bgg.request.thread
import org.audux.bgg.request.user
import org.audux.bgg.response.Family
import org.audux.bgg.response.Response
import org.audux.bgg.response.Thing
import org.jetbrains.annotations.VisibleForTesting

/**
 * Unofficial Board Game Geek API Client for the
 * [BGG XML2 API2](https://boardgamegeek.com/wiki/page/BGG_XML_API2).
 *
 * Search example usage:
 * ```
 * BggClient
 *      .search("Scythe", arrayOf(ThingType.BOARD_GAME, ThingType.BOARD_GAME_EXPANSION))
 *      .call { response -> println(response) }
 * ```
 */
object BggClient {
    init {
        setLoggerSeverity(Severity.Warn)
    }

    @VisibleForTesting var engine = { CIO.create() }

    /**
     * Request details about a user's collection.
     *
     * NOTE: The default (or using [subType]=[ThingType.BOARD_GAME]) returns both
     * [ThingType.BOARD_GAME] and [ThingType.BOARD_GAME_EXPANSION] in the collection... BUT
     * incorrectly marks the [subType] as [ThingType.BOARD_GAME] for the expansions. Workaround is
     * to use [excludeSubType]=[ThingType.BOARD_GAME_EXPANSION] and make a 2nd call asking for
     * [subType]=[ThingType.BOARD_GAME_EXPANSION]
     *
     * @param userName Name of the user to request the collection for
     * @param subType Specifies which collection you want to retrieve - only one type at the time
     * @param excludeSubType Specifies which subtype you want to exclude from the results.
     * @param ids Filter collection to specifically listed item(s).
     * @param version Returns version info for each item in your collection.
     * @param brief Returns more abbreviated results.
     * @param stats Returns expanded rating/ranking info for the collection.
     * @param own Filter for owned games. Set to [Inclusion.EXCLUDE] to exclude these items so
     *   marked. Set to [Inclusion.INCLUDE] for returning owned games and 0 for non-owned games.
     * @param rated Filter for whether an item has been rated. Set to [Inclusion.EXCLUDE] to exclude
     *   these items so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
     * @param played Filter for whether an item has been played. Set to [Inclusion.EXCLUDE] to
     *   exclude these items so marked. Set to [Inclusion.INCLUDE] to include only these items so
     *   marked.
     * @param comment Filter for items that have been commented. Set to [Inclusion.EXCLUDE] to
     *   exclude these items so marked. Set to [Inclusion.INCLUDE] to include only these items so
     *   marked.
     * @param trade Filter for items marked for trade. Set to [Inclusion.EXCLUDE] to exclude these
     *   items so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
     * @param want Filter for items wanted in trade. Set to [Inclusion.EXCLUDE] to exclude these
     *   items so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
     * @param wishlist Filter for items on the wishlist. Set to [Inclusion.EXCLUDE] to exclude these
     *   items so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
     * @param wishlistPriority Filter for wishlist priority. Returns only items of the specified
     *   priority. Range 1-5.
     * @param preOrdered Filter for pre-ordered games. Set to [Inclusion.EXCLUDE] to exclude these
     *   items so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
     * @param wantToPlay Filter for items marked as wanting to play. Set to [Inclusion.EXCLUDE] to
     *   exclude these items so marked. Set to [Inclusion.EXCLUDE] to include only these items so
     *   marked.
     * @param wantToBuy Filter for ownership flag. Set to [Inclusion.EXCLUDE] to exclude these items
     *   so marked. Set to [Inclusion.INCLUDE] to include only these items so marked.
     * @param previouslyOwned Filter for games marked previously owned. Set to [Inclusion.EXCLUDE]
     *   to exclude these items so marked. Set to [Inclusion.INCLUDE] to include only these items so
     *   marked.
     * @param hasParts Filter on whether there is a comment in the Has Parts field of the item. Set
     *   to [Inclusion.EXCLUDE] to exclude these items so marked. Set to [Inclusion.INCLUDE] to
     *   include only these items so marked.
     * @param wantParts Filter on whether there is a comment in the Wants Parts field of the item.
     *   Set to [Inclusion.EXCLUDE] to exclude these items so marked. Set to [Inclusion.INCLUDE] to
     *   include only these items so marked.
     * @param minRating Filter on minimum personal rating assigned for that item in the collection.
     *   Range 1-10.
     * @param rating Filter on maximum personal rating assigned for that item in the collection.
     *   Rang 1-10
     * @param minBggRating Filter on minimum BGG rating for that item in the collection. Range 1-10
     *   (Or -1). NOTE: 0 is ignored. You can use -1 e.g. min -1 and max 1 to get items with no bgg
     *   rating.
     * @param bggRating Filter on maximum BGG rating for that item in the collection. Range 1-10.
     * @param minimumPlays Filter by minimum number of recorded plays.
     * @param maxPlays Filter by maximum number of recorded plays.
     * @param collectionId Restrict the collection results to the single specified collection id.
     *   `Collid` is returned in the results of normal queries as well.
     * @param modifiedSince Restricts the collection results to only those whose status (own, want,
     *   fortrade, etc.) has changed or been added since the date specified (does not return results
     *   for deletions).
     */
    @JvmStatic
    fun collection(
        userName: String,
        subType: ThingType,
        excludeSubType: ThingType? = null,
        ids: Array<Int>? = null,
        version: Boolean = false,
        brief: Boolean = false,
        stats: Boolean = false,
        own: Inclusion? = null,
        rated: Inclusion? = null,
        played: Inclusion? = null,
        comment: Inclusion? = null,
        trade: Inclusion? = null,
        want: Inclusion? = null,
        wishlist: Inclusion? = null,
        wishlistPriority: Int? = null,
        preOrdered: Inclusion? = null,
        wantToPlay: Inclusion? = null,
        wantToBuy: Inclusion? = null,
        previouslyOwned: Inclusion? = null,
        hasParts: Inclusion? = null,
        wantParts: Inclusion? = null,
        minRating: Int? = null,
        rating: Int? = null,
        minBggRating: Int? = null,
        bggRating: Int? = null,
        minimumPlays: Int? = null,
        maxPlays: Int? = null,
        collectionId: Int? = null,
        modifiedSince: LocalDateTime? = null
    ) =
        InternalBggClient(engine)
            .collection(
                userName,
                subType,
                excludeSubType,
                ids,
                version,
                brief,
                stats,
                own,
                rated,
                played,
                comment,
                trade,
                want,
                wishlist,
                wishlistPriority,
                preOrdered,
                wantToPlay,
                wantToBuy,
                previouslyOwned,
                hasParts,
                wantParts,
                minRating,
                rating,
                minBggRating,
                bggRating,
                minimumPlays,
                maxPlays,
                collectionId,
                modifiedSince
            )

    /**
     * Family thing endpoint that retrieve details about the given family ID and associated `Link`
     * objects.
     *
     * @param ids array of IDs returning only families of the specified id.
     * @param types Single [HotListType] returning only items of the specified type, defaults to
     *   [HotListType.BOARD_GAME].
     */
    @JvmStatic
    fun familyItems(ids: Array<Int>, types: Array<FamilyType> = arrayOf()) =
        InternalBggClient(engine).familyItems(ids, types)

    /**
     * Retrieves the list of available forums for the given id / type combination. e.g. Retrieve all
     * the available forums for `[id=342942, type=thing]` i.e. Ark nova.
     *
     * @param id The id of either the Family or Thing to retrieve
     * @param type Single [ForumListType] to retrieve, either a [Thing] or [Family]
     */
    @JvmStatic
    fun forumList(id: Int, type: ForumListType) = InternalBggClient(engine).forumList(id, type)

    /**
     * Retrieves the list of threads for the given forum id.
     *
     * Note: Pagination data is not returned in the response but can be calculated by
     * `Math.ceil(numThreads/50)`.
     *
     * @param id The id of the forum.
     * @param page Used to paginate, this is the page that is returned, only 50 threads per page are
     *   returned. Note that page 0 and 1 are the same.
     */
    @JvmStatic fun forum(id: Int, page: Int? = null) = InternalBggClient(engine).forum(id, page)

    /**
     * Geek list endpoint, retrieves a specific geek list by its ID.
     *
     * NOTE: This request returns a (http) 202 the first time the request is made.
     *
     * @param id the unique ID for the geek list to retrieve
     * @param comments whether to include the comments in the response or not.
     */
    @JvmStatic
    fun geekList(id: Int, comments: Inclusion? = null) =
        InternalBggClient(engine).geekList(id, comments)

    /**
     * Retrieve information about the given guild (id) like name, description, members etc.
     *
     * @param id ID of the guild you want to view.
     * @param members Include member roster in the results. Member list is paged and sorted.
     * @param sort Specifies how to sort the members list; default is username.
     * @param page The page of the members list to return. page size is 25.
     */
    @JvmStatic
    fun guilds(id: Int, members: Inclusion? = null, sort: String? = null, page: Int? = null) =
        InternalBggClient(engine).guilds(id, members, sort, page)

    /**
     * Hotness endpoint that retrieve the list of most 50 active items on the site filtered by type.
     *
     * @param type Single [HotListType] returning only items of the specified type, defaults to
     *   [HotListType.BOARD_GAME].
     */
    @JvmStatic fun hotItems(type: HotListType? = null) = InternalBggClient(engine).hotItems(type)

    /**
     * Request a list of plays (max 100 at the time) for the given user.
     *
     * @param username Name of the player you want to request play information for. Data is returned
     *   in backwards-chronological form. You must include either a username or an id and type to
     *   get results.
     * @param id Id number of the item you want to request play information for. Data is returned in
     *   backwards-chronological form.
     * @param type Type of the item you want to request play information for. Valid types include:
     *   thing family
     * @param minDate Returns only plays of the specified date or later.
     * @param maxDate Returns only plays of the specified date or earlier.
     * @param subType=TYPE Limits play results to the specified TYPE; boardgame is the default.
     * @param page The page of information to request. Page size is 100 records.
     */
    @JvmStatic
    fun plays(
        username: String,
        id: Int? = null,
        type: PlayThingType? = null,
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
        subType: SubType? = null,
        page: Int? = null,
    ) = InternalBggClient(engine).plays(username, id, type, minDate, maxDate, subType, page)

    /**
     * Search endpoint that allows searching by name for things on BGG.
     *
     * @param query Returns all types of items that match [query]. Spaces in the SEARCH_QUERY are
     *   replaced by a
     * @param types Returns all items that match SEARCH_QUERY of type [ThingType]. You can return
     *   multiple types by using more.
     * @param exactMatch Limit results to items that match the [query] exactly
     */
    @JvmStatic
    fun search(
        query: String,
        types: Array<ThingType> = arrayOf(),
        exactMatch: Boolean = false,
    ) = InternalBggClient(engine).search(query, types, exactMatch)

    /**
     * Request a Thing or list of things. Multiple things can be requested by passing in several
     * IDs. At least one ID is required to make this request. Sending along [types] might result in
     * an empty as the API filters based on the [ThingType].
     *
     * @param ids Specifies the id of the thing(s) to retrieve. To request multiple things with a
     *   single query, can specify a comma-delimited list of ids.
     * @param types Specifies that, regardless of the type of thing asked for by id, the results are
     *   filtered by the [ThingType] objects specified. Leave empty to return all types.
     * @param stats Returns ranking and rating stats for the thing.
     * @param versions Returns version info for the thing.
     * @param videos Returns videos for the thing.
     * @param marketplace Returns marketplace data.
     * @param comments Returns all comments about the thing. Also includes ratings when commented.
     *   See page parameter.
     * @param ratingComments Returns all ratings for the thing. Also includes comments when rated.
     *   See page parameter. The [ratingComments] and [comments] parameters cannot be used together,
     *   as the output always appears in the <comments> node of the XML; comments parameter takes
     *   precedence if both are specified. Ratings are sorted in descending rating value, based on
     *   the highest rating they have assigned to that thing (each thing in the collection can have
     *   a different rating).
     * @param page Defaults to 1, controls the page of data to see for comments, and ratings data.
     * @param pageSize Set the number of records to return in paging. Minimum is 10, maximum is 100.
     *   Defaults to 100.
     */
    @JvmStatic
    fun things(
        ids: Array<Int>,
        types: Array<ThingType> = arrayOf(),
        stats: Boolean = false,
        versions: Boolean = false,
        videos: Boolean = false,
        marketplace: Boolean = false,
        comments: Boolean = false,
        ratingComments: Boolean = false,
        page: Int = 1,
        pageSize: Int? = null,
    ) =
        InternalBggClient(engine)
            .things(
                ids,
                types,
                stats,
                versions,
                videos,
                marketplace,
                comments,
                ratingComments,
                page,
                pageSize
            )

    /**
     * Retrieves the list of articles/posts for the given thread.
     *
     * @param id The id of the thread.
     * @param minArticleId Filters the results so that only articles with an equal or higher id than
     *   NNN will be returned.
     * @param minArticleDate Filters the results so that only articles after the specified date an
     *   time (HH:MM:SS) or later will be returned.
     * @param count Limits the number of articles returned to no more than NNN.
     */
    @JvmStatic
    fun thread(
        id: Int,
        minArticleId: Int? = null,
        minArticleDate: LocalDateTime? = null,
        count: Int? = null
    ) = InternalBggClient(engine).thread(id, minArticleId, minArticleDate, count)

    /**
     * User endpoint that retrieves a specific user by their [name].
     *
     * @param name Specifies the user name (only one user is request-able at a time).
     * @param buddies Turns on buddies reporting. Results are paged; see page parameter.
     * @param guilds Turns on optional guilds reporting. Results are paged; see page parameter.
     * @param hot Include the user's hot 10 list from their profile. Omitted if empty.
     * @param top Include the user's top 10 list from their profile. Omitted if empty.
     * @param domain Controls the domain for the users hot 10 and top 10 lists. The DOMAIN default
     *   is boardgame; valid values are: boardgame, rpg, or videogame
     * @param page Specifies the page of buddy and guild results to return. The default page is 1 if
     *   you don't specify it; page size is 100 records (Current implementation seems to return 1000
     *   records). The page parameter controls paging for both buddies and guilds list if both are
     *   specified. If a <buddies> or <guilds> node is empty, it means that you have requested a
     *   page higher than that needed to list all the buddies/guilds or, if you're on page 1, it
     *   means that that user has no buddies and is not part of any guilds.
     */
    @JvmStatic
    fun user(
        name: String,
        buddies: Inclusion? = null,
        guilds: Inclusion? = null,
        top: Inclusion? = null,
        hot: Inclusion? = null,
        domain: Domains? = null,
        page: Int? = null,
    ) = InternalBggClient(engine).user(name, buddies, guilds, top, hot, domain, page)

    /** Logging level Severity for the BGGClient logging. */
    enum class Severity {
        Verbose,
        Debug,
        Info,
        Warn,
        Error,
        Assert
    }

    /** Sets the Logger severity defaults to [Severity.Error] */
    @JvmStatic
    fun setLoggerSeverity(severity: Severity) {
        Logger.setMinSeverity(
            when (severity) {
                Severity.Assert -> co.touchlab.kermit.Severity.Assert
                Severity.Debug -> co.touchlab.kermit.Severity.Debug
                Severity.Error -> co.touchlab.kermit.Severity.Error
                Severity.Info -> co.touchlab.kermit.Severity.Info
                Severity.Verbose -> co.touchlab.kermit.Severity.Verbose
                Severity.Warn -> co.touchlab.kermit.Severity.Warn
            }
        )
    }

    /** Internal BGG Client containing the actual implementations of the API Calls. */
    internal class InternalBggClient(private val engine: () -> HttpClientEngine) {
        private val clientScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        internal val client = {
            HttpClient(engine()) {
                install(ClientRateLimitPlugin) { requestLimit = 25 }
                install(HttpRequestRetry) {
                    exponentialDelay()
                    retryIf(maxRetries = 10) { request, response ->
                        response.status.value.let {
                            // Add 429 (TooManyRequests) and 202 (Accepted) for retries, see:
                            // https://boardgamegeek.com/thread/1188687/export-collections-has-been-updated-xmlapi-develop
                            (it in (500..599) + 202 + 429).also { shouldRetry ->
                                if (shouldRetry) {
                                    Logger.i("HttpRequestRetry") {
                                        "Got status code $it Retrying request[${request.url}"
                                    }
                                }
                            }
                        }
                    }
                }

                expectSuccess = true
            }
        }

        val mapper: ObjectMapper =
            XmlMapper.builder()
                .apply {
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)

                    addModule(JacksonXmlModule())
                    addModule(JavaTimeModule())
                    addModule(
                        KotlinModule.Builder()
                            .enable(KotlinFeature.NullToEmptyCollection)
                            .enable(KotlinFeature.StrictNullChecks)
                            .build()
                    )

                    // Keep hardcoded to US: https://bugs.openjdk.org/browse/JDK-8251317
                    // en_GB Locale uses 'Sept' as a shortname when formatting dates (e.g. 'MMM').
                    // The
                    // locale en_US remains 'Sep'.
                    defaultLocale(Locale.US)
                    defaultMergeable(true)
                    defaultUseWrapper(false)
                }
                .build()

        /**
         * Calls/Launches a request async, once a response is available it will call
         * [responseCallback].
         */
        internal fun <T> callAsync(request: suspend () -> T, responseCallback: (T) -> Unit) =
            clientScope.launch {
                val response = request()
                withContext(Dispatchers.Default) { responseCallback(response) }
            }

        /** Calls/Launches a request and returns it's response. */
        @OptIn(DelicateCoroutinesApi::class)
        internal fun <T> callAsync(request: suspend () -> Response<T>) =
            GlobalScope.future { request() }

        /** Calls/Launches a request and returns it's response. */
        internal suspend fun <T> call(request: suspend () -> Response<T>) = request()

        /** Returns a wrapped request for later execution. */
        internal fun <T> request(request: suspend () -> Response<T>) = Request(this, request)
    }
}

/** Thrown whenever any exception is thrown during a request to BGG. */
class BggRequestException(message: String) : Exception(message)
