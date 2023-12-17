package org.audux.bgg.response

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.truth.Truth
import org.audux.bgg.module.BggXmlObjectMapper
import org.audux.bgg.module.appModule
import org.audux.bgg.util.TestUtils
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

class HotResponseTest : KoinTest {
    @JvmField
    @RegisterExtension
    @Suppress("unused")
    val koinTestExtension = KoinTestExtension.create { modules(appModule) }

    private val mapper: ObjectMapper by inject(named<BggXmlObjectMapper>())

    @Test
    fun `Parses empty response`() {
        val results = mapper.readValue(TestUtils.xml("hot?type=rpgperson"), HotList::class.java)

        Truth.assertThat(results.results).hasSize(0)
    }

    @Test
    fun `Parses a hot list`() {
        val results = mapper.readValue(TestUtils.xml("hot"), HotList::class.java)

        Truth.assertThat(results.results).hasSize(50)
        Truth.assertThat(results.results[0])
            .isEqualTo(
                HotListItem(
                    id = 332686,
                    rank = 1,
                    name = WrappedValue("John Company: Second Edition"),
                    yearPublished = WrappedValue(2022),
                    thumbnail =
                        WrappedValue(
                            "https://cf.geekdo-images.com/TAdE4z_bwAAjJlmPrkmKhA__thumb/img/pwgtQn8ArKjwBxk3bnDuIVAPWgU=/fit-in/200x150/filters:strip_icc()/pic6601629.jpg"
                        ),
                )
            )
    }
}
