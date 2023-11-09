package org.audux.bgg

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendPathSegments
import io.ktor.util.StringValues
import kotlinx.coroutines.runBlocking
import org.audux.bgg.data.response.Items

class BggClient {
    private val client = HttpClient(OkHttp) {
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 5)
            exponentialDelay()
        }

        expectSuccess = true
    }

    suspend fun things(
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
         * The [ratingcomments] and [comments] parameters cannot be used together, as the output
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


        return client.get(BASE_URL) {
            url {
                appendPathSegments(PATH_THING)

                parameters.appendAll(
                    StringValues.build {
                        append(PARAM_ID, ids.joinToString(","))

                        if (types.isNotEmpty()) {
                            append(PARAM_TYPE, types.joinToString { "${it.param}," })
                        }

                        if (stats) append(PARAM_STATS, "1")
                        if (versions) append(PARAM_VERSIONS, "1")
                        if (videos) append(PARAM_VIDEOS, "1")
                        if (marketplace) append(PARAM_MARKETPLACE, "1")
                        if (comments) append(PARAM_COMMENTS, "1")
                        if (ratingComments) append(PARAM_RATING_COMMENTS, "1")
                        if (page > 1) append(PARAM_PAGE, page.toString())
                        if (pageSize != 100) append(PARAM_PAGE_SIZE, pageSize.toString())
                    }
                )
            }
        }
    }

    fun close() {
        client.close()
    }

    private companion object {
        const val BASE_URL = "https://boardgamegeek.com/xmlapi2"

        const val PATH_THING = "thing"

        const val PARAM_ID = "id"
        const val PARAM_TYPE = "type"
        const val PARAM_VERSIONS = "versions"
        const val PARAM_VIDEOS = "videos"
        const val PARAM_STATS = "stats"
        const val PARAM_MARKETPLACE = "marketplace"
        const val PARAM_COMMENTS = "comments"
        const val PARAM_RATING_COMMENTS = "ratingcomments"
        const val PARAM_PAGE = "page"
        const val PARAM_PAGE_SIZE = "pagesize"

        @JvmStatic
        fun main(args: Array<String>) {
            Napier.base(DebugAntilog())

            val client = BggClient()
            runBlocking {
                val response = client.things(
                    ids = arrayOf(1),
                    types = arrayOf(ThingType.BOARD_GAME),
                    ratingComments = true,
                    page = 1,
                    pageSize = 100
                )

                val xmlDeserializer = XmlMapper(JacksonXmlModule().apply {
                    setDefaultUseWrapper(false)
                }).registerKotlinModule()
                    .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

                Napier.i(
                    xmlDeserializer.readValue(response.bodyAsText(), Items::class.java).toString()
                )


            }

            client.close()
        }
    }

    enum class ThingType(val param: String) {
        BOARD_GAME("boardgame"),
        BOARD_GAME_EXPANSION("boardgameexpansion"),
        BOARD_GAME_ACCESSORY("boardgameaccessory"),
        VIDEO_GAME("videogame"),
        RPG_ITEM("rpgitem"),
        RPG_ISSUE("rpgissue")

    }
}

class BggRequestException(message: String) : Exception(message)
