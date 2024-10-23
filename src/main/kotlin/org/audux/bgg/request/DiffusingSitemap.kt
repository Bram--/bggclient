package org.audux.bgg.request

import co.touchlab.kermit.Logger
import java.util.concurrent.ConcurrentHashMap
import org.audux.bgg.InternalBggClient
import org.audux.bgg.common.SitemapLocationType
import org.audux.bgg.response.Response
import org.audux.bgg.response.SitemapIndex
import org.audux.bgg.response.SitemapUrl

/**
 * Handles request diffusion/fanning out for the [sitemapIndex]. After making the initial request to
 * the [sitemapIndex] calling [diffuse] causes it to make additional (parallel) requests. The
 * additional requests are the sitemaps that are returned from the index and a map is then build
 * with the returned [SitemapUrl] objects.
 *
 * Furthermore, a [SitemapLocationType] can be provided to prevent requesting any sitemap that is
 * not of that type.
 */
class DiffusingSitemap
internal constructor(
    private val client: InternalBggClient,
    private val request: suspend () -> Response<SitemapIndex>,
) : Request<SitemapIndex>(client, request) {

    /**
     * After requesting the [SitemapIndex] all URLs that are of the given type are requested. If
     * [limitToTypes] is not set _all_ sitemaps will be requested. this will result in 600+ requests
     * to BGG!
     *
     * @param limitToTypes The type of sitemaps to request e.g. if [SitemapLocationType.BOARD_GAMES]
     *   is set it will only request sitemaps that contain board games, like
     *   `https://boardgamegeek.com/sitemap_geekitems_boardgame_page_15`.
     */
    fun diffuse(
        vararg limitToTypes: SitemapLocationType
    ): Request<Map<SitemapLocationType, List<SitemapUrl>>> =
        Request(client) {
            // Run the initial sitemap index request.
            request().let { sitemapIndex ->
                if (sitemapIndex.data == null) return@Request Response(error = sitemapIndex.error)
                val allSitemaps = ConcurrentHashMap<SitemapLocationType, MutableList<SitemapUrl>>()
                // Filter sitemaps by given types.
                val sitemaps =
                    sitemapIndex.data.sitemaps.filter {
                        limitToTypes.isEmpty() || limitToTypes.contains(it.type)
                    }

                // Start requesting all the sitemap concurrently.
                concurrentRequests(sitemaps.indices) { index ->
                    val sitemap = sitemaps[index]
                    val response = client.sitemap(sitemap.location).call()

                    if (response.data == null || response.isError()) {
                        Logger.w("Error retrieving ${sitemap.location}")
                    } else {
                        // Add all URLs to the sitemaps hash map.
                        allSitemaps.compute(sitemap.type) { _, value ->
                            (value ?: mutableListOf()).apply { addAll(response.data.sitemaps) }
                        }
                    }
                }

                // Finally build the response manually.
                Response(data = allSitemaps.toMap())
            }
        }
}
