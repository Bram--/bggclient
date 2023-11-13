package org.audux.bgg.data.request

import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.appendPathSegments
import io.ktor.util.StringValues
import org.audux.bgg.BggClient
import org.audux.bgg.BggClient.ThingType
import org.audux.bgg.BggRequestException

/**
 * Request a Thing or list of things. Multiple items can be requested by passing in several IDs.
 * At least one ID is required to make this request. Sending along [types] might result in an empty
 * as the API filters based on the [ThingType].
 *
 *
 */
suspend fun BggClient.things(
    /**
     * Specifies the id of the thing(s) to retrieve. To request multiple things with a single
     * query, can specify a comma-delimited list of ids.
     */
    ids: Array<Int>,

    /**
     * Specifies that, regardless of the type of thing asked for by id, the results are
     * filtered by the THINGTYPE(s) specified. Multiple THINGTYPEs can be specified in
     * a comma-delimited list. Leave empty to return all types.
     * @see ThingType
     */
    types: Array<ThingType> = arrayOf(),

    /** Returns ranking and rating stats for the item. */
    stats: Boolean = false,

    /** Returns version info for the item. */
    versions: Boolean = false,

    /** Returns videos for the item. */
    videos: Boolean = false,

    /** Returns marketplace data. */
    marketplace: Boolean = false,

    /**
     * Returns all comments about the item. Also includes ratings when commented.
     * See page parameter.
     */
    comments: Boolean = false,

    /**
     * Returns all ratings for the item. Also includes comments when rated. See page parameter.
     * The [ratingComments] and [comments] parameters cannot be used together, as the output
     * always appears in the <comments> node of the XML; comments parameter takes precedence if
     * both are specified.
     *
     * Ratings are sorted in descending rating value, based on the highest rating they have
     * assigned to that item (each item in the collection can have a different rating).
     */
    ratingComments: Boolean = false,

    /** Defaults to 1, controls the page of data to see for comments, and ratings data. */
    page: Int = 1,

    /**
     * Set the number of records to return in paging. Minimum is 10, maximum is 100.
     * Defaults to 100
     */
    pageSize: Int = 100,
): HttpResponse {
    if (!(10..100).contains(pageSize)) {
        throw BggRequestException("pageSize must be between 10 and 100")
    }
    if (comments && ratingComments) {
        throw BggRequestException("comments and ratingsComments can't both be true.")
    }

    return client.get(BggClient.BASE_URL) {
        url {
            appendPathSegments(BggClient.PATH_THING)

            parameters.appendAll(
                StringValues.build {
                    append(BggClient.PARAM_ID, ids.joinToString(","))

                    if (types.isNotEmpty()) {
                        append(BggClient.PARAM_TYPE, types.joinToString { "${it.param}," })
                    }

                    if (stats) append(BggClient.PARAM_STATS, "1")
                    if (versions) append(BggClient.PARAM_VERSIONS, "1")
                    if (videos) append(BggClient.PARAM_VIDEOS, "1")
                    if (marketplace) append(BggClient.PARAM_MARKETPLACE, "1")
                    if (comments) append(BggClient.PARAM_COMMENTS, "1")
                    if (ratingComments) append(BggClient.PARAM_RATING_COMMENTS, "1")
                    if (page > 1) append(BggClient.PARAM_PAGE, page.toString())
                    if (pageSize != 100) append(BggClient.PARAM_PAGE_SIZE, pageSize.toString())
                }
            )
        }
    }
}
