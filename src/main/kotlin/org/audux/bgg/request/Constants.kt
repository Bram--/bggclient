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

/** Collection of constants used in creating an URL for the BGG API. */
internal object Constants {
    /** Base URL for the XML API2. */
    const val BASE_URL = "https://boardgamegeek.com/xmlapi2"

    const val PATH_COLLECTION = "collection"
    const val PATH_FAMILY = "family"
    const val PATH_FORUM_LIST = "forumlist"
    const val PATH_FORUM = "forum"
    const val PATH_HOT = "hot"
    const val PATH_SEARCH = "search"
    const val PATH_THING = "thing"

    const val PARAM_BGG_RATING = "bggrating"
    const val PARAM_BRIEF = "brief"
    const val PARAM_COLLECTION_ID = "collid"
    const val PARAM_COMMENT = "comment"
    const val PARAM_COMMENTS = "comments"
    const val PARAM_EXACT = "exact"
    const val PARAM_EXCLUDE_SUBTYPE = "excludesubtype"
    const val PARAM_ID = "id"
    const val PARAM_HAS_PARTS = "hasparts"
    const val PARAM_MARKETPLACE = "marketplace"
    const val PARAM_MAX_PLAYS = "maxplays"
    const val PARAM_MINIMUM_PLAYS = "minplays"
    const val PARAM_MINIMUM_RATING = "minrating"
    const val PARAM_MINIMUM_BGG_RATING = "minbggrating"
    const val PARAM_MODIFIED_SINCE = "modifiedsince"
    const val PARAM_OWN = "own"
    const val PARAM_PAGE = "page"
    const val PARAM_PAGE_SIZE = "pagesize"
    const val PARAM_PLAYED = "played"
    const val PARAM_PRE_ORDERED = "preordered"
    const val PARAM_PREVIOUSLY_OWNED = "prevowned"
    const val PARAM_QUERY = "query"
    const val PARAM_RATED = "rated"
    const val PARAM_RATING = "rating"
    const val PARAM_RATING_COMMENTS = "ratingcomments"
    const val PARAM_STATS = "stats"
    const val PARAM_SUBTYPE = "subtype"
    const val PARAM_TRADE = "trade"
    const val PARAM_TYPE = "type"
    const val PARAM_USERNAME = "username"
    const val PARAM_VERSION = "version"
    const val PARAM_VERSIONS = "versions"
    const val PARAM_VIDEOS = "videos"
    const val PARAM_WANT = "want"
    const val PARAM_WANT_PARTS = "wantparts"
    const val PARAM_WANT_TO_BUY = "wanttobuy"
    const val PARAM_WANT_TO_PLAY = "wanttoplay"
    const val PARAM_WISHLIST = "wishlist"
    const val PARAM_WISHLIST_PRIORITY = "wishlistpriority"
}
