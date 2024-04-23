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
import io.ktor.client.engine.cio.CIO
import java.time.LocalDate
import java.time.LocalDateTime
import org.audux.bgg.common.Domain
import org.audux.bgg.common.FamilyType
import org.audux.bgg.common.ForumListType
import org.audux.bgg.common.HotListType
import org.audux.bgg.common.Inclusion
import org.audux.bgg.common.PlayThingType
import org.audux.bgg.common.SubType
import org.audux.bgg.common.ThingType
import org.audux.bgg.request.collection
import org.audux.bgg.request.familyItems
import org.audux.bgg.request.forum
import org.audux.bgg.request.forumList
import org.audux.bgg.request.geekList
import org.audux.bgg.request.guild
import org.audux.bgg.request.hotList
import org.audux.bgg.request.plays
import org.audux.bgg.request.search
import org.audux.bgg.request.sitemapIndex
import org.audux.bgg.request.things
import org.audux.bgg.request.thread
import org.audux.bgg.request.user
import org.audux.bgg.response.Family
import org.audux.bgg.response.Response
import org.audux.bgg.response.Thing
import org.jetbrains.annotations.VisibleForTesting

/**
 * BggClient is a API client for the
 * [Board Game Geek XML(1) API](https://boardgamegeek.com/wiki/page/BGG_XML_API) and
 * [Board Game Geek XML2 API](https://boardgamegeek.com/wiki/page/BGG_XML_API2). These APIs work for
 * all geek domains, meaning Board games, video games and RPGs can be retrieved. It works on both
 * the JVM and Android (24+). Before using the any of these BGG APIs please refer to the
 * [Terms of Use](https://boardgamegeek.com/wiki/page/XML_API_Terms_of_Use#) for the APIs. Finally
 * If you're looking for all Board game IDs and some basic information please refer to
 * [this page](https://boardgamegeek.com/data_dumps/bg_ranks) that contains CSV with all boardgames
 * instead.
 *
 * **A short summary of the APIs available:** Clicking through the API documentation will have more
 * information as well as example code.
 *
 * |API           |Description                                                                                                                                                                                                                                                                         |
 * |--------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
 * |[collection]  |Request details about a user's collection. Returns a (partial) [org.audux.bgg.response.Collection]                                                                                                                                                                                  |
 * |[familyItems] |Family thing endpoint that retrieve details about the given family ID and associated `Link` objects. Returns a [org.audux.bgg.response.Family]                                                                                                                                      |
 * |[forumList]   |Retrieves the list of available forums for the given id / type combination. e.g. Retrieve all the available forums for `id=342942, type=thing` i.e. Ark nova. Returns a [org.audux.bgg.response.ForumList]                                                                          |
 * |[forum]       |Retrieves the list of threads for the given forum id. Returns a [org.audux.bgg.response.Forum]                                                                                                                                                                                      |
 * |[geekList]    |Retrieves a specific geek list by its ID, returning its items and optionally comments left on the GeekList. Returns a [org.audux.bgg.response.GeekList]                                                                                                                             |
 * |[guild]       |Retrieve information about the given guild (id) like name, description, members etc. Returns a [org.audux.bgg.response.Guild]                                                                                                                                                       |
 * |[hotList]     |Hotness endpoint that retrieve the list of most 50 hot/active items on the site filtered by type. Returns a [org.audux.bgg.response.HotList]                                                                                                                                        |
 * |[plays]       |Request a list of plays (max 100 at the time) for the given user. Returns a [org.audux.bgg.response.Plays]                                                                                                                                                                          |
 * |[search]      |Search endpoint that allows searching by name for things on BGG. Returns [org.audux.bgg.response.SearchResults]                                                                                                                                                                     |
 * |[sitemapIndex]|Requests the Sitemap index for the given Domain. Call `org.audux.bgg.request.DiffusingSitemap.diffuse` to request specific sitemaps. Returns a [org.audux.bgg.response.SitemapIndex]                                                                                                |
 * |[things]      |Request a Thing or list of things. Multiple things can be requested by passing in several IDs. At least one ID is required to make this request. Sending along `types` might result in an empty as the API filters based on the `ThingType`. Returns [org.audux.bgg.response.Things]|
 * |[thread]      |Retrieves the list of articles/posts for the given thread. Returns a [org.audux.bgg.response.Thread]                                                                                                                                                                                |
 * |[user]        |User endpoint that retrieves a specific user by their `name`. Returns a [org.audux.bgg.response.User]                                                                                                                                                                               |
 */
object BggClient {
    init {
        setLoggerSeverity(Severity.Warn)
    }

    /** @suppress */
    @VisibleForTesting @JvmStatic var engine = { CIO.create() }

    /** @suppress */
    internal var configuration = BggClientConfiguration()

    /**
     * Allows configuration of requests strategies of the BggClient.
     *
     * @see BggClientConfiguration
     */
    @JvmStatic
    fun configure(block: BggClientConfiguration.() -> Unit) {
        configuration = BggClientConfiguration().apply { block.invoke(this) }
    }

    /**
     * Request details about a user's collection and returning a
     * [org.audux.bgg.response.Collection].
     *
     * Requesting the only played board games _and_ board game expansions for the user can be done
     * as follows:
     * ```
     * Response<User> playedGames =
     *      BggClient.collection(userName = "user", played = Inclusion.INCLUDE).call()
     * println(playedGames.data) // Prints all played games in their collection.
     * ```
     *
     * The actual data class returned is [Request<Collection>](org.audux.bgg.request.Request) which
     * can then be used to make the actual call to the API using `call` or `callAsync`. This will
     * then return a [Response] and wrap a data class (on success). e.g. the above example might
     * return a [org.audux.bgg.response.Collection] looking something as follows:
     * ```
     *   Collection(totalItems = 2, publishDate = '23-04-2024', items = listOf(
     *      CollectionItem(collectionId = 100, objectId = 12345, name = "Example 1", ....),
     *      CollectionItem(collectionId = 101, objectId = 12325, name = "Example 2", ....)
     *   )
     * ```
     *
     * NOTE: The default (or using [subType]=[ThingType.BOARD_GAME]) returns both
     * [ThingType.BOARD_GAME] and [ThingType.BOARD_GAME_EXPANSION] in the collection... BUT
     * incorrectly marks the [subType] as [ThingType.BOARD_GAME] for the expansions. Workaround is
     * to use [excludeSubType]=[ThingType.BOARD_GAME_EXPANSION] and make a 2nd call asking for
     * [subType]=[ThingType.BOARD_GAME_EXPANSION].
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
    @JvmOverloads
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
        InternalBggClient()
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
     * Family items endpoint that retrieve details about the given family ID and associated `Link`
     * objects. The data class returned from the API call is a [org.audux.bgg.response.FamilyItem].
     * The family items (ids)/types are returned from the [things] endpoint where they might appear
     * as a link on the `Thing`.
     *
     * Requesting more information about the family with ID 50152 can be done as follows:
     * ```
     * Response<Family> familyItems =
     *      BggClient.familyItems(ids = arrayOf(50152)).call()
     * println(familyItems.data) // Prints information about the family and associated links.
     * ```
     *
     * The actual data class returned is [Request<Family>](org.audux.bgg.request.Request) which can
     * then be used to make the actual call to the API using `call` or `callAsync`. This will then
     * return a [Response] and wrap a data class (on success). e.g. the above example might return a
     * [org.audux.bgg.response.Family] looking something as follows:
     * ```
     *   Family(items = listOf(
     *      FamilyItem(id = 50152, familyType = FamilyType.BOARD_GAME_FAMILY, links = listOf(
     *          Link(id="65901",value="Age of Industry", type = FamilyType.BOARD_GAME_FAMILY)
     *   )))
     * ```
     *
     * @param ids array of IDs returning only families of the specified id.
     * @param types Single [FamilyType] returning only items of the specified type, defaults to
     *   [FamilyType.BOARD_GAME_FAMILY].
     */
    @JvmStatic
    @JvmOverloads
    fun familyItems(ids: Array<Int>, types: Array<FamilyType> = arrayOf()) =
        InternalBggClient().familyItems(ids, types)

    /**
     * Retrieves the list of threads for the given forum id in a [org.audux.bgg.response.Forum].
     *
     * Requesting the first 50 threads for Ark Nova review forum can be done as follows:
     * ```
     * Response<Forum> forum = BggClient.forum(id = 3696791).call()
     * println(forum.data) // Prints a list of threads (max 50)
     * ```
     *
     * If all threads should be retrieved pagination can be used as follows:
     * ```
     * Response<Forum> forum = BggClient.forum(id = 3696791).paginate().call()
     * println(forum.data) // Prints a list of ALL threads
     * ```
     *
     * The actual class returned is [org.audux.bgg.request.PaginatedForum]) which can then be used
     * to paginate or make the actual call to the API using `call` or `callAsync`. This will then
     * return a [Response] and wrap a data class (on success). e.g. the above example might return a
     * [org.audux.bgg.response.Forum] looking something as follows:
     * ```
     * ForumList(id = 123, title = "Reviews", numThreads = 300, threads = listOf(
     *      ThreadSummary(
     *          id = 12345,
     *          subject = "Reviews are cool",
     *          userName = "posterUserName",
     *          lastPostDate = LocalDateTime, // "Tue, 23 Jan 2024 09:13:43 +0000"
     *          numArticles = 65),
     *          // etc...
     * ))
     * ```
     *
     * Note: Pagination data is not returned in the response but can be calculated by
     * `Math.ceil(numThreads/50)`.
     *
     * @param id The id of the forum.
     * @param page Used to paginate, this is the page that is returned, only 50 threads per page are
     *   returned. Note that page 0 and 1 are the same.
     */
    @JvmStatic
    @JvmOverloads
    fun forum(id: Int, page: Int? = null) = InternalBggClient().forum(id, page)

    /**
     * Retrieves the list of available forums for the given id / type combination, returning a
     * [org.audux.bgg.response.ForumList].
     *
     * Requesting all forums for Ark Nova can be done as follows:
     * ```
     * Response<ForumList> forumList =
     *      BggClient.forumList(id = 342942, type = ForumListType.THING).call()
     * println(forumList.data) // Prints a list of forum summaries for Ark Nova
     * ```
     *
     * The actual data class returned is [Request<ForumList>](org.audux.bgg.request.Request) which
     * can then be used to make the actual call to the API using `call` or `callAsync`. This will
     * then return a [Response] and wrap a data class (on success). e.g. the above example might
     * return a [org.audux.bgg.response.ForumList] looking something as follows:
     * ```
     * ForumList(id = 342942, type = ForumListType.THING, forums = listOf(
     *      ForumSummary(
     *          id = 3696791,
     *          title = "Reviews",
     *          description = "Post your game reviews ....",
     *          lastPostDate = LocalDateTime, // "Tue, 23 Jan 2024 09:13:43 +0000"
     *          numPosts = 1603,
     *          numThreads = 65),
     *          // etc...
     * ))
     * ```
     *
     * @param id The id of either the Family or Thing to retrieve
     * @param type Single [ForumListType] to retrieve, either a [Thing] or [Family]
     */
    @JvmStatic fun forumList(id: Int, type: ForumListType) = InternalBggClient().forumList(id, type)

    /**
     * Geek list endpoint, retrieves a specific geek list by its ID and return a
     * [org.audux.bgg.response.GeekList].
     *
     * Requesting more information about the Geek list with ID 331520 _and_ retrieving all its
     * comment can be done as follows:
     * ```
     * Response<GeekList> geekList = BggClient.geekList(id = 331520).call()
     * println(geekList.data) // Prints information about the family and associated links.
     * ```
     *
     * The actual data class returned is [Request<GeekList>](org.audux.bgg.request.Request) which
     * can then be used to make the actual call to the API using `call` or `callAsync`. This will
     * then return a [Response] and wrap a data class (on success). e.g. the above example might
     * return a [org.audux.bgg.response.GeekList] looking something as follows:
     * ```
     *   GeekList(id = 221520, title = "title",
     *      items = listOf(
     *          GeekListItem(
     *              id = 104,
     *              objectType = "thing",
     *              subType = SubType.BOARD_GAME,
     *              objectName = "Board game name"
     *          ),
     *          // etc...
     *      ),
     *      comments = listOf(
     *          GeekListComment(userName = "Name", value = "Awesome!", thumbs = 10),
     *          // etc...
     *      ),
     *   )
     * ```
     *
     * NOTE: This request returns a (http) 202 the first time the request is made.
     *
     * @param id the unique ID for the geek list to retrieve
     * @param comments whether to include the comments in the response or not.
     */
    @JvmStatic
    @JvmOverloads
    fun geekList(id: Int, comments: Inclusion? = null) = InternalBggClient().geekList(id, comments)

    /**
     * Retrieve information about the given guild (id) like name, description, members etc.
     * returning a [org.audux.bgg.response.Guild].
     *
     * Requesting the more information about a specific ID including its members(limited to 25) can
     * be done as follows:
     * ```
     * Response<Guild> guild = BggClient.guild(id = 123, members = Inclusion.INCLUDE).call()
     * println(guild.data) // Prints a the guild information and the first 25 guild members.
     * ```
     *
     * If all guild members should be retrieved, pagination can be used as follows:
     * ```
     * Response<Guild> guild = BggClient
     *      .guild(id = 123, members = Inclusion.INCLUDE).paginate().call()
     * println(guild.data) // Prints a the guild information and ALL guild members.
     * ```
     *
     * The actual class returned is [org.audux.bgg.request.PaginatedGuilds]) which can then be used
     * to paginate or make the actual call to the API using `call` or `callAsync`. This will then
     * return a [Response] and wrap a data class (on success). e.g. the above pagination example
     * might return a [org.audux.bgg.response.Guild] looking something as follows:
     * ```
     * Guild(
     *  id = 123,
     *  name ="Guild name,
     *  created = LocalDateTime,
     *  // ...
     *  members = GuildMembers(
     *      count = 62,
     *      page = 3, // Or 1 if pagination is not used.
     *      members = listOf(
     *          GuildMember(name = "userName", joinDate = LocalDateTime),
     *          // +61 more if pagination is used...
     *      )
     * )
     * ```
     *
     * @param id ID of the guild you want to view.
     * @param members Include member roster in the results. Member list is paged and sorted.
     * @param sort Specifies how to sort the members list; default is username.
     * @param page The page of the members list to return. page size is 25.
     */
    @JvmStatic
    @JvmOverloads
    fun guild(id: Int, members: Inclusion? = null, sort: String? = null, page: Int? = null) =
        InternalBggClient().guild(id, members, sort, page)

    /**
     * Hotness endpoint that retrieve the list of most 50 active items on the site filtered by type,
     * returning a [org.audux.bgg.response.HotList].
     *
     * Requesting the top 50 'hottest' board games right now can be done as follows:
     * ```
     * Response<HotList> hotList = BggClient.hotList(type = HotListType.BOARD_GAME).call()
     * println(familyItems.data) // Prints a list of HotListItem with some info about the board game
     * ```
     *
     * The actual data class returned is [Request<Hot>](org.audux.bgg.request.Request) which can
     * then be used to make the actual call to the API using `call` or `callAsync`. This will then
     * return a [Response] and wrap a data class (on success). e.g. the above example might return a
     * [org.audux.bgg.response.HotList] looking something as follows:
     * ```
     * HotList(items = listOf(
     *  ListItem(
     *      id = 123,
     *      rank = 1,
     *      name = "The HOTTEST game",
     *      thumbnail = "http://...",
     *      yearPublished = 1999
     *  ),
     *  // +49 items...
     * )
     * ```
     *
     * @param type Single [HotListType] returning only items of the specified type, defaults to
     *   [HotListType.BOARD_GAME].
     */
    @JvmStatic
    @JvmOverloads
    fun hotList(type: HotListType? = null) = InternalBggClient().hotList(type)

    /**
     * Request a list of plays (max 100 at the time) for the given user, returning
     * [org.audux.bgg.response.Plays].
     *
     * Requesting a list of plays after 2022 for a specific username be done as follows:
     * ```
     * Response<Plays> plays = BggClient
     *      .plays(username = "user", minDate = LocalDate.of(2023, 1, 1)).call()
     * println(plays.data) // Prints a Plays object with a list of (max 100) Play objects.
     * ```
     *
     * If the number of plays exceeds 100 not all would've been retrieved with the above API call.
     * In order to do so pagination can be used:
     * ```
     * Response<Plays> plays = BggClient
     *      .plays(username = "user", minDate = LocalDate.of(2023, 1, 1)).paginate().call()
     * println(plays.data) // Prints a Plays object with a list of ALL Play objects.
     * ```
     *
     * The actual class returned is [org.audux.bgg.request.PaginatedPlays]) which can then be used
     * to paginate or make the actual call to the API using `call` or `callAsync`. This will then
     * return a [Response] and wrap a data class (on success). e.g. the above pagination example
     * might return a [org.audux.bgg.response.Plays] looking something as follows:
     * ```
     * Plays(
     *      username = "user",
     *      userid="12345",
     *      total = 240,
     *      page = 1, // 1 if no pagination or last page when paginating
     *      plays = listOf(
     *          Play(
     *              id = 45678,
     *              date = LocalDate,
     *              quantity = 2,
     *              lengthInMinutes = 120,
     *              item = PlayItem(name = "Earth", objectId = 350184, /** ... */),
     *              players = listOf(),
     *          ),
     *          // etc...
     *     )
     * )
     * ```
     *
     * @param username Name of the player you want to request play information for. Data is returned
     *   in backwards-chronological form. You must include either a username or an id and type to
     *   get results.
     * @param id Id number of the item you want to request play information for. Data is returned in
     *   backwards-chronological form.
     * @param type Type of the item you want to request play information for. Valid types include:
     *   [PlayThingType].
     * @param minDate Returns only plays of the specified date or later.
     * @param maxDate Returns only plays of the specified date or earlier.
     * @param subType=TYPE Limits play results to the specified TYPE; boardgame is the default.
     * @param page The page of information to request. Page size is 100 records.
     */
    @JvmStatic
    @JvmOverloads
    fun plays(
        username: String,
        id: Int? = null,
        type: PlayThingType? = null,
        minDate: LocalDate? = null,
        maxDate: LocalDate? = null,
        subType: SubType? = null,
        page: Int? = null,
    ) = InternalBggClient().plays(username, id, type, minDate, maxDate, subType, page)

    /**
     * Search endpoint that allows searching by name for things on BGG return a
     * [org.audux.bgg.response.SearchResults].
     *
     * Search for a board game start with 'My little' can be done as follows:
     * ```
     * Response<searchResults> searchResults = BggClient
     *      .search(query = 'My Little', type = arrayOf(ThingType.BOARD_GAME)).call()
     * println(searchResults.data) // Prints an object with a list of 140+ SearchResult objects.
     * ```
     *
     * The actual data class returned is [Request<SearchResults>](org.audux.bgg.request.Request)
     * which can then be used to make the actual call to the API using `call` or `callAsync`. This
     * will then return a [Response] and wrap a data class (on success). e.g. the above example
     * might return a [org.audux.bgg.response.SearchResults] looking something as follows:
     * ```
     *   SearchResults(total = 144, results = listOf(
     *      SearchResult(
     *          name = Name(type = "primary" value = "Connect 4: My Little Pony"),
     *          id = 167159,
     *          type = ThingType.BOARD_GAME,
     *          yearpublished = 2014,
     *      ),
     *      // etc...
     *   )
     * ```
     *
     * @param query Returns all types of items that match [query]. Spaces in the SEARCH_QUERY are
     *   replaced by a
     * @param types Returns all items that match SEARCH_QUERY of type [ThingType]. You can return
     *   multiple types by using more.
     * @param exactMatch Limit results to items that match the [query] exactly
     */
    @JvmStatic
    @JvmOverloads
    fun search(
        query: String,
        types: Array<ThingType> = arrayOf(),
        exactMatch: Boolean = false,
    ) = InternalBggClient().search(query, types, exactMatch)

    /**
     * Requests the Sitemap index for the given Domain. Call
     * [org.audux.bgg.request.DiffusingSitemap.diffuse] to request specific sitemaps, returning
     * either a [org.audux.bgg.response.SitemapIndex] OR (when using `diffuse`) a map of
     * [org.audux.bgg.response.SitemapUrl] keyed by [org.audux.bgg.common.SitemapLocationType].
     *
     * For example requesting all sitemaps in the Sitemap index can be done as follows:
     * ```
     * val sitemapIndex = BggClient.sitemapIndex().call()
     * ```
     *
     * For requesting only a type of sitemap and immediately request the URLs in it (opposed to a
     * link to the sitemap), can be done as follows. Here a request is made to retrieve all board
     * game pages:
     * ```
     * val boardGameUrls = BggClient.sitemapIndex().diffuse(SitemapLocationType.BOARD_GAMES).call()
     * ```
     *
     * The actual class returned is [org.audux.bgg.request.DiffusingSitemap]) which can then be used
     * to request ALL sitemaps or make the actual call to the API using `call` or `callAsync`. This
     * will then return a [Response] and wrap a data class (on success). e.g. the above pagination
     * example might return a [org.audux.bgg.response.SitemapIndex]. When using `diffuse()` the
     * response might looks something like:
     * ```
     * Map<SitemapLocationType, List<SitemapLocationUrl>) response = mapOf(
     *      SitemapLocationType.BOARD_GAMES to listOf(
     *          SitemapUrl(location = "https://boardgamegeek.com/boardgame/1/die-macher", ...),
     *          SitemapUrl(location = "https://boardgamegeek.com/boardgame/2/dragonmaster", ...),
     *          SitemapUrl(location = "https://boardgamegeek.com/boardgame/3/samurai", ...),
     *      ),
     *      // etc...
     * )
     * ```
     *
     * @param domain The [Domain] to request the sitemap for [Domain.BOARD_GAME_GEEK] returns board
     *   game related sitemaps, using [Domain.VIDEO_GAME_GEEK] will return video game related
     *   sitemaps. All returned data/ids are usable with BggClient APIs. E.g. a video game sitemap
     *   might contain an URL like `https://videogamegeek.com/videogame/68287/master-orion`. This ID
     *   `68287` can be used to request more information using the [things] API - regardless of its
     *   type.
     */
    @JvmStatic
    fun sitemapIndex(domain: Domain = Domain.BOARD_GAME_GEEK) =
        InternalBggClient().sitemapIndex(domain)

    /**
     * Request a Thing or list of things. Multiple things can be requested by passing in several
     * IDs. At least one ID is required to make this request. Sending along [types] might result in
     * an empty as the API filters based on the [ThingType]. This returns
     * [org.audux.bgg.response.Things].
     *
     * Requesting detailed information about Scythe and Ark Nova can be done as follows:
     * ```
     * Response<Things> things = BggClient.things(
     *      ids = arrayOf(169786, 342942),
     *      stats = true,
     *      versions = true,
     *      videos = true,
     *      marketplace = false,
     *      comments = false
     *  )
     * // Prints a detailed information (including marketplace data, comments etc.) of
     * // both Scythe and Ark Nova. Only containing the first 100 comments of each
     * println(plays.data)
     * ```
     *
     * If the number of comments exceeds `pageSize` (default 100) not all comments would be
     * retrieved with the above API call. In order to do so pagination can be used. When paginating
     * the last page is whichever thing has the most comments e.g. if Ark Nova has 200 comments and
     * Scythe has 1000, 9 additional requests will be made and the last page(in the response data
     * classes) would be 10,
     * ```
     * Response<Things> things = BggClient.things(
     *      ids = arrayOf(169786, 342942),
     *      stats = true,
     *      versions = true,
     *      videos = true,
     *      marketplace = false,
     *      comments = false
     *  ).paginate()
     * // Prints a detailed information (including marketplace data, comments etc.) of
     * // both Scythe and Ark Nova. Only containing ALL comments of both.
     * println(plays.data)
     * ```
     *
     * The actual class returned is [org.audux.bgg.request.PaginatedThings]) which can then be used
     * to paginate or make the actual call to the API using `call` or `callAsync`. This will then
     * return a [Response] and wrap a data class (on success). e.g. the above pagination example
     * might return a [org.audux.bgg.response.Things] looking something as follows:
     * ```
     * Things(
     *      username = "user",
     *      things = listOf(
     *          id = 169786,
     *          name = "Scythe", // Primary name
     *          names = listOf(Name(...)),
     *          polls = listOf(
     *              LanguageDependencePoll(
     *                  name = "Language Dependence",
     *                  votes = 900,
     *                  results = listOf(
     *                      LeveledPollResult(level = 1 value="..." numberOfVotes = 6),
     *                      LeveledPollResult(level = 2 value="..." numberOfVotes = 16),
     *                      // etc....
     *                  )
     *              ),
     *              PlayerAgePoll(
     *                  name = "User Suggested Player Age",
     *                  votes = 200,
     *                  results = listOf(
     *                      PollResult(value="2", numberOfVotes = 0),
     *                      // ... etc.
     *                      PollResult(value="21 and up", numberOfVotes = 120),
     *                  )
     *              ),
     *              NumberOfPlayersPoll(
     *                  name = "User Suggested Number of Players",
     *                  votes = 50,
     *                  results = listOf(
     *                      NumberOfPlayerResults(
     *                          numberOfPlayers = "1",
     *                          results = listOf(
     *                              PollResult(value = "Best", numberOfVotes = 3)
     *                              PollResult(value = "Recommended", numberOfVotes = 24)
     *                              PollResult(value = "Not Recommended", numberOfVotes = 12)
     *                          ),
     *                      ),
     *                      // etc...
     *                  )
     *              ),
     *
     *              // And much more!
     *          ),
     *      ),
     *      things = listOf(
     *          id = 342942,
     *          name = "Ark Nova", // Primary name
     *          names = listOf(Name(...)),
     *          polls = listOf( ..... )
     *          // And much more..
     *      )
     * )
     * ```
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
    @JvmOverloads
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
        InternalBggClient()
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
     * Retrieves the list of articles/posts for the given thread - requesting ALL articles/posts.
     * Returning [org.audux.bgg.response.Thread].
     *
     * If all threads should be retrieved pagination can be used as follows:
     * ```
     * Response<Thread> thread = BggClient.thread(id = 3208373).paginate().call()
     * println(forum.data) // Prints thread details and a list of `articles`
     * ```
     *
     * The actual class returned is [org.audux.bgg.request.Request]) which can then be used to
     * paginate or make the actual call to the API using `call` or `callAsync`. This will then
     * return a [Response] and wrap a data class (on success). e.g. the above example might return a
     * [org.audux.bgg.response.Thread] looking something as follows:
     * ```
     * Thread(
     *      id = 3208373,
     *      subject = "New Maps for Ark Nova + Marine World",
     *      link = "https://boardgamegeel.com/...",
     *      numArticles = 300,
     *      articles = listOf(
     *          Article(
     *              id = 43461362,
     *              username = "username"
     *              link = "https://boardgamegeek.com/thread/3208373/article/43461362#43461362",
     *              postdate = LocalDateTime,
     *              editdate = LocalDateTime
     *              numedits = 4>
     *              subject = "New Maps for Ark Nova + Marine World"
     *              body = "Hi. Here are 3 new maps for the community:...."
     *           ),
     *           // etc..,
     *      )
     * )
     * ```
     *
     * Note: Pagination can be achieved using the `count` and `minArticleId` but is not implemented
     * by this library. As a result some API calls may return a 500, 502 or 408 as there are too
     * many posts.
     *
     * @param id The id of the thread.
     * @param minArticleId Filters the results so that only articles with an equal or higher id than
     *   NNN will be returned.
     * @param minArticleDate Filters the results so that only articles after the specified date an
     *   time (HH:MM:SS) or later will be returned.
     * @param count Limits the number of articles returned to no more than NNN.
     */
    @JvmStatic
    @JvmOverloads
    fun thread(
        id: Int,
        minArticleId: Int? = null,
        minArticleDate: LocalDateTime? = null,
        count: Int? = null
    ) = InternalBggClient().thread(id, minArticleId, minArticleDate, count)

    /**
     * User endpoint that retrieves a specific user by their [name] returning a
     * [org.audux.bgg.response.User].
     *
     * Retrieving a user including their guilds, friends/buddies, personal hot- and top-list.
     *
     * ```
     * Response<User> user =
     *      BggClient.user(
     *          name = "username",
     *          buddies = Inclusion.INCLUDE,
     *          guilds = Inclusion.INCLUDE,
     *          hot = Inclusion.INCLUDE,
     *          top = Inclusion.INCLUDE
     *      ).paginate().call()
     * println(forum.data) // Prints a list of buddies and guilds threads
     * ```
     *
     * The actual class returned is [org.audux.bgg.request.PaginatedUser]) which can then be used to
     * paginate or make the actual call to the API using `call` or `callAsync`. This will then
     * return a [Response] and wrap a data class (on success). e.g. the above example might return a
     * [org.audux.bgg.response.User] looking something as follows:
     * ```
     * User(
     *      id = 123,
     *      username = "Username",
     *      firstname = "Firstname",
     *      lastname = "Surname",
     *      avatarLink = "N/A",
     *      yearRegistered = 2020,
     *      lastLogin = LocalDate,
     *      buddies = Buddies(
     *          total = 100,
     *          buddies = listOf(
     *              Buddy(name = "BuddyUserName", id = 123),
     *              // etc..
     *          )
     *      ),
     *      guilds = Guilds(
     *          total = 100,
     *          guilds = listOf(
     *              GuildReference(name = "Board Games Club" id= 1234),
     *              // etc..
     *          )
     *      ),
     *      // etc...
     * ))
     * ```
     *
     * Note: Pagination is unlikely to be needed as the page size (number of guilds & buddies
     * returned) is 1000.
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
    @JvmOverloads
    fun user(
        name: String,
        buddies: Inclusion? = null,
        guilds: Inclusion? = null,
        top: Inclusion? = null,
        hot: Inclusion? = null,
        domain: Domain? = null,
        page: Int? = null,
    ) = InternalBggClient().user(name, buddies, guilds, top, hot, domain, page)

    /**
     * Logging level Severity for the BGGClient logging.
     *
     * @suppress
     */
    enum class Severity {
        Verbose,
        Debug,
        Info,
        Warn,
        Error
    }

    /** Sets the Logger severity defaults to [Severity.Error] */
    @JvmStatic
    fun setLoggerSeverity(severity: Severity) {
        Logger.setMinSeverity(
            when (severity) {
                Severity.Debug -> co.touchlab.kermit.Severity.Debug
                Severity.Error -> co.touchlab.kermit.Severity.Error
                Severity.Info -> co.touchlab.kermit.Severity.Info
                Severity.Verbose -> co.touchlab.kermit.Severity.Verbose
                Severity.Warn -> co.touchlab.kermit.Severity.Warn
            }
        )
    }
}

/**
 * Configure the BGGClient request strategies i.e. how many concurrent requests are allowed and how
 * should retying be done.
 *
 * Specifies an exponential delay between retries, is done using [retryBase], [retryMaxDelayMs] and
 * [retryRandomizationMs] This is then calculated using the Exponential backoff algorithm: delay
 * equals to `retryBase ^ retryAttempt * 1000 + [0..randomizationMs]`
 *
 * @property maxConcurrentRequests How many requests can be active at the same time.
 * @property maxRetries How many retries per URL should be tried.
 * @property retryBase see kdoc for formula
 * @property retryMaxDelayMs see kdoc for formula
 * @property retryRandomizationMs see kdoc for formula
 * @property requestTimeoutMillis At which point requests time out/throw an time out Exception.
 */
data class BggClientConfiguration(
    var maxConcurrentRequests: Int = 10,
    var maxRetries: Int = 5,
    var retryBase: Double = 2.0,
    var retryMaxDelayMs: Long = 60_000,
    var retryRandomizationMs: Long = 1_000,
    var requestTimeoutMillis: Long = 15_000
)

/** Thrown whenever any exception is thrown during a request to BGG. */
class BggRequestException(message: String) : Exception(message)
