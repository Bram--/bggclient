package org.audux.bgg

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import kotlinx.coroutines.runBlocking
import org.audux.bgg.data.request.ThingType
import org.audux.bgg.data.request.things
import org.audux.bgg.module.BggKtorClient
import org.audux.bgg.module.BggXmlObjectMapper
import org.audux.bgg.module.appModule
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.qualifier.named

class BggClient : KoinComponent {
    internal val client : HttpClient by inject(named<BggKtorClient>())
    internal val mapper : ObjectMapper by inject(named<BggXmlObjectMapper>())

    init {
        startKoin {
            modules(appModule)
        }
    }

    fun close() {
        client.close()
    }

    internal companion object {
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
                    ids = arrayOf(224517),
                    types = arrayOf(ThingType.BOARD_GAME),
                    marketplace = true,
                    ratingComments = true,
                    videos = true,
                    versions = true,
                    stats = true,
                )

                val game = response.things[0]
                Napier.i("""
                    Got board game: ${game.name[0]}
                    ${game.description}
                    
                    versions: ${game.versions.size}
                    
                    """.trimIndent())
            }

            client.close()
        }
    }

}

class BggRequestException(message: String) : Exception(message)
