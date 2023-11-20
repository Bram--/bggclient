package org.audux.bgg.data.response

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.truth.Truth.assertThat
import java.io.InputStream
import java.time.LocalDateTime
import org.audux.bgg.module.BggXmlObjectMapper
import org.audux.bgg.module.appModule
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.RegisterExtension
import org.koin.core.component.inject
import org.koin.core.qualifier.named
import org.koin.test.KoinTest
import org.koin.test.junit5.KoinTestExtension

/** Test class for [Things] and nested response classes. */
class ThingsResponseTest : KoinTest {
  @JvmField
  @RegisterExtension
  val koinTestExtension = KoinTestExtension.create { modules(appModule) }

  private val mapper: ObjectMapper by inject(named<BggXmlObjectMapper>())

  @Test
  fun `parses a simple response`() {
    val things = mapper.readValue(xml("response.things.boardgame.minimal"), Things::class.java)

    assertThat(things.things).hasSize(1)
    val thing = things.things[0]
    assertThat(thing.names[0].type).isEqualTo("primary")
    assertThat(thing.names[0].value).isEqualTo("Die Macher")

    assertThat(thing.description).hasLength(1270)
    assertThat(thing.yearPublished?.value).isEqualTo(1986)
    assertThat(thing.minPlayers?.value).isEqualTo(3)
    assertThat(thing.maxPlayers?.value).isEqualTo(5)
    assertThat(thing.playingTimeInMinutes?.value).isEqualTo(240)
    assertThat(thing.minPlayingTimeInMinutes?.value).isEqualTo(240)
    assertThat(thing.maxPlayingTimeInMinutes?.value).isEqualTo(240)
    assertThat(thing.minAge?.value).isEqualTo(14)
  }

  @Test
  fun `parses polls`() {
    val things = mapper.readValue(xml("response.things.boardgame.minimal"), Things::class.java)

    assertThat(things.things).hasSize(1)
    val thing = things.things[0]
    assertThat(thing.polls).hasSize(3)

    assertThat(thing.polls.map { it.javaClass })
        .containsExactly(
            PlayerAgePoll::class.java,
            LanguageDependencePoll::class.java,
            NumberOfPlayersPoll::class.java)
  }

  @Test
  fun `parses poll PlayerAgePoll`() {
    val things = mapper.readValue(xml("response.things.boardgame.minimal"), Things::class.java)

    val thing = things.things[0]
    val playerAgePoll = thing.polls.findLast { it is PlayerAgePoll }!! as PlayerAgePoll

    assertThat(playerAgePoll.title).isEqualTo("User Suggested Player Age")
    assertThat(playerAgePoll.totalVotes).isEqualTo(31)
    val groupedVotes = playerAgePoll.results
    assertThat(groupedVotes).hasSize(12)
    assertThat(groupedVotes[0].value).isEqualTo("2")
    assertThat(groupedVotes[0].numberOfVotes).isEqualTo(0)
    assertThat(groupedVotes[1].value).isEqualTo("3")
    assertThat(groupedVotes[1].numberOfVotes).isEqualTo(0)
    assertThat(groupedVotes[2].value).isEqualTo("4")
    assertThat(groupedVotes[2].numberOfVotes).isEqualTo(0)
    assertThat(groupedVotes[3].value).isEqualTo("5")
    assertThat(groupedVotes[3].numberOfVotes).isEqualTo(0)
    assertThat(groupedVotes[4].value).isEqualTo("6")
    assertThat(groupedVotes[4].numberOfVotes).isEqualTo(0)
    assertThat(groupedVotes[5].value).isEqualTo("8")
    assertThat(groupedVotes[5].numberOfVotes).isEqualTo(0)
    assertThat(groupedVotes[6].value).isEqualTo("10")
    assertThat(groupedVotes[6].numberOfVotes).isEqualTo(0)
    assertThat(groupedVotes[7].value).isEqualTo("12")
    assertThat(groupedVotes[7].numberOfVotes).isEqualTo(6)
    assertThat(groupedVotes[8].value).isEqualTo("14")
    assertThat(groupedVotes[8].numberOfVotes).isEqualTo(18)
    assertThat(groupedVotes[9].value).isEqualTo("16")
    assertThat(groupedVotes[9].numberOfVotes).isEqualTo(4)
    assertThat(groupedVotes[10].value).isEqualTo("18")
    assertThat(groupedVotes[10].numberOfVotes).isEqualTo(2)
    assertThat(groupedVotes[11].value).isEqualTo("21 and up")
    assertThat(groupedVotes[11].numberOfVotes).isEqualTo(1)
  }
  @Test
  fun `parses poll LanguageDependencePoll`() {
    val things = mapper.readValue(xml("response.things.boardgame.minimal"), Things::class.java)

    val thing = things.things[0]
    val languageDependencePoll = thing.polls.findLast { it is LanguageDependencePoll }!! as LanguageDependencePoll

    assertThat(languageDependencePoll.title).isEqualTo("Language Dependence")
    assertThat(languageDependencePoll.totalVotes).isEqualTo(48)
    val groupedVotes = languageDependencePoll.results
    //TODO: Finish writing test.
  }

  @Test
  fun `WrappedValue Parses self closing elements - Int`() {
    val xml = """<item value="100" />""""
    val wrappedInt = mapper.readValue(xml, object : TypeReference<WrappedValue<Int>>() {})

    assertThat(wrappedInt.value).isEqualTo(100)
  }

  @Test
  fun `WrappedValue Parses self closing elements - String`() {
    val xml = """<item value="Hello" />""""
    val wrappedInt = mapper.readValue(xml, object : TypeReference<WrappedValue<String>>() {})

    assertThat(wrappedInt.value).isEqualTo("Hello")
  }

  @Test
  fun `WrappedLocalDateTime Parses BGG specific date format`() {
    val localDateXml = """<item value="Sun, 13 Sep 2020 10:43:49 +0000" />""""
    val localDateTime = mapper.readValue(localDateXml, WrappedLocalDateTime::class.java)

    assertThat(localDateTime.value).isEqualTo(LocalDateTime.of(2020, 9, 13, 10, 43, 49))
  }

  companion object {
    fun xml(fileName: String): InputStream {
      return Companion::class.java.classLoader.getResourceAsStream("xml/$fileName.xml")!!
    }
  }
}
