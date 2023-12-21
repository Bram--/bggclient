package org.audux.bgg.request

import com.google.common.truth.Truth.assertThat
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respondOk
import io.ktor.http.Headers
import io.ktor.http.HttpMethod
import io.ktor.http.Parameters
import io.ktor.http.URLBuilder
import io.ktor.http.URLProtocol
import java.time.LocalDateTime
import kotlinx.coroutines.runBlocking
import org.audux.bgg.BggClient
import org.audux.bgg.common.Inclusion
import org.audux.bgg.common.ThingType
import org.audux.bgg.module.BggHttpEngine
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test
import org.koin.core.qualifier.named
import org.koin.dsl.module
import org.koin.test.KoinTest

class CollectionRequestTest : KoinTest {
    @Test
    fun `Makes a request with parameters`() {
        runBlocking {
            val mockEngine = MockEngine {
                respondOk(
                    String(
                        TestUtils.xml(
                                "collection?username=novaeux&stats=1&subtype=boardgame&excludesubtype=boardgameexpansion"
                            )
                            .readAllBytes()
                    )
                )
            }
            val client = BggClient()
            client
                .getKoin()
                .loadModules(
                    listOf(
                        module {
                            single(named<BggHttpEngine>()) {
                                // Not useless as mockEngine needs to be bound to HttpClientEngine
                                // and not set up a new binding for HttpClientEngine
                                @Suppress("USELESS_CAST")
                                mockEngine as HttpClientEngine
                            }
                        }
                    )
                )
            val response =
                client
                    .collection(
                        userName = "Noveaux",
                        subType = ThingType.BOARD_GAME,
                        excludeSubType = ThingType.BOARD_GAME_EXPANSION,
                        ids = arrayOf(1, 2, 3),
                        version = true,
                        brief = true,
                        stats = true,
                        own = Inclusion.INCLUDE,
                        rated = Inclusion.INCLUDE,
                        played = Inclusion.INCLUDE,
                        comment = Inclusion.INCLUDE,
                        trade = Inclusion.INCLUDE,
                        want = Inclusion.INCLUDE,
                        wishlist = Inclusion.INCLUDE,
                        wishlistPriority = 5,
                        preOrdered = Inclusion.INCLUDE,
                        wantToPlay = Inclusion.INCLUDE,
                        wantToBuy = Inclusion.INCLUDE,
                        previouslyOwned = Inclusion.INCLUDE,
                        hasParts = Inclusion.INCLUDE,
                        wantParts = Inclusion.INCLUDE,
                        minRating = 1,
                        rating = 4,
                        minBggRating = 2,
                        minimumPlays = 1,
                        maxPlays = 99,
                        collectionId = -1,
                        modifiedSince = LocalDateTime.MIN,
                    )
                    .call()

            val request = mockEngine.requestHistory[0]
            assertThat(mockEngine.requestHistory).hasSize(1)
            assertThat(request.method).isEqualTo(HttpMethod.Get)
            assertThat(request.headers)
                .isEqualTo(
                    Headers.build {
                        appendAll("Accept", listOf("*/*"))
                        appendAll("Accept-Charset", listOf("UTF-8"))
                    }
                )
            val expectedUrl =
                URLBuilder(
                        protocol = URLProtocol.HTTPS,
                        host = "boardgamegeek.com",
                        pathSegments = listOf("xmlapi2", "collection"),
                        parameters =
                            Parameters.build {
                                append("username", "Noveaux")
                                append("subtype", ThingType.BOARD_GAME.param)
                                append("excludesubtype", ThingType.BOARD_GAME_EXPANSION.param)
                                append("id", arrayOf(1, 2, 3).joinToString(","))
                                append("version", "1")
                                append("brief", "1")
                                append("stats", "1")
                                append("own", "1")
                                append("rated", "1")
                                append("played", "1")
                                append("comment", "1")
                                append("trade", "1")
                                append("want", "1")
                                append("wishlist", "1")
                                append("wishlistpriority", "5")
                                append("preordered", "1")
                                append("wanttoplay", "1")
                                append("wanttobuy", "1")
                                append("prevowned", "1")
                                append("hasparts", "1")
                                append("wantparts", "1")
                                append("minrating", "1")
                                append("rating", "4")
                                append("minbggrating", "2")
                                append("minplays", "1")
                                append("maxplays", "99")
                                append("collid", "-1")
                                append("modifiedsince", "00-01-01 00:00:00")
                            }
                    )
                    .build()
            assertThat(request.url).isEqualTo(expectedUrl)
            assertThat(response.items).hasSize(105)
            client.close()
        }
    }
}
