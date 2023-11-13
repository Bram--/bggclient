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
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.runBlocking
import org.audux.bgg.data.request.things
import org.audux.bgg.data.response.Things

class BggClient {
    internal val client = HttpClient(OkHttp) {
        install(HttpRequestRetry) {
            retryOnServerErrors(maxRetries = 5)
            exponentialDelay()
        }

        expectSuccess = true
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
                    ids = arrayOf(1),
                    types = arrayOf(ThingType.BOARD_GAME),
                    ratingComments = true,
                    page = 1,
                    pageSize = 100
                )

                val xmlDeserializer = XmlMapper.builder().apply {
                    configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
                    configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

                    addModule(JacksonXmlModule())

                    defaultUseWrapper(false)
                }.build().registerKotlinModule()

                Napier.i(
                    xmlDeserializer.readValue(response.bodyAsText(), Things::class.java).toString()
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
