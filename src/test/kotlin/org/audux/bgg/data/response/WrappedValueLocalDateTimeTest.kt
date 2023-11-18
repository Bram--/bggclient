package org.audux.bgg.data.response

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.truth.Truth.assertThat
import org.audux.bgg.module.BggXmlObjectMapper
import org.audux.bgg.module.appModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension
import java.time.LocalDateTime

/** Test class for [Things] and nested response classes. */
class ThingsResponseTest : KoinTest {
    @JvmField
    @RegisterExtension
    val koinTestExtension = KoinTestExtension.create { modules(appModule) }

    private val mapper: ObjectMapper by inject(named<BggXmlObjectMapper>())

    @Test
    fun `Parses BGG specific date format`() {
        val localDateXml = """<item value="Sun, 13 Sep 2020 10:43:49 +0000" />""""
        val localDateTime = mapper.readValue(localDateXml, WrappedValueLocalDateTime::class.java)

        assertThat(localDateTime.value)
            .isEqualTo(
                LocalDateTime
                    .of(2020, 9, 13, 9, 43, 49)
            )
    }
}