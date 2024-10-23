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
package org.audux.bgg.response

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.annotation.JsonSetter
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.serialization.Serializable
import org.audux.bgg.common.Link
import org.audux.bgg.common.Name
import org.audux.bgg.common.Statistics
import org.audux.bgg.common.ThingType

/** Response wrapper for the things to be returned. */
@Serializable
@JsonRootName("items")
data class Things(
    /** Terms of use of the BGG API. */
    @JacksonXmlProperty(isAttribute = true) val termsOfUse: String,

    /** List of the actual things. */
    @JacksonXmlProperty(localName = "item") val things: List<Thing>,
)

/**
 * An item or thing which could be either of the [org.audux.bgg.common.ThingType] objects. As a
 * result of the loose overlap of the types most values are Nullable, however type specific data is
 * supplied via the [Link] property.
 *
 * Furthermore, the settings/filling of the properties is highly dependent on the request. This is
 * because additional parameters need to be set in order for the data to be retrieved. For example
 * making a things request with simply an `id` parameter will not return any videos, comments,
 * marketplace data, statistics or version information.
 *
 * @see org.audux.bgg.request.things
 */
@Serializable
data class Thing(
    /** Unique BGG identifier. */
    val id: Int,
    val error: String?,

    /**
     * The type of thing e.g. boardgame, expansion etc.
     *
     * @see org.audux.bgg.common.ThingType
     */
    @JsonDeserialize(using = ThingTypeDeserializer::class) val type: ThingType?,

    /** URL to 200 by 150 thumbnail image. */
    @JsonDeserialize(using = TrimmedStringDeserializer::class) val thumbnail: String?,

    /** URL to full sized image. */
    @JsonDeserialize(using = TrimmedStringDeserializer::class) val image: String?,

    /** Long form description of the thing. */
    @JsonDeserialize(using = TrimmedStringDeserializer::class) val description: String?,

    /** The year it was published in e.g. `2019`. */
    @JsonDeserialize(using = WrappedIntDeserializer::class) val yearPublished: Int?,

    /**
     * The date it was published (For RPG-issues).
     *
     * Note: Most publish dates only contain years and have an invalid date format, i.e. '1999-0-0'
     */
    @JsonDeserialize(using = WrappedStringDeserializer::class) val datePublished: String?,

    /** The year it was released in e.g. `2019`. (For video games) */
    @JsonDeserialize(using = WrappedLocalDateDeserializer::class)
    @Serializable(with = LocalDateSerializer::class)
    val releaseDate: LocalDate?,

    /** Minimum number of players required. */
    @JsonDeserialize(using = WrappedIntDeserializer::class) val minPlayers: Int?,

    /** Maximum number of players. */
    @JsonDeserialize(using = WrappedDoubleDeserializer::class) val maxPlayers: Double?,

    /** How many minutes on average it takes to complete the thing/game. */
    @JacksonXmlProperty(localName = "playingtime")
    @JsonDeserialize(using = WrappedIntDeserializer::class)
    val playingTimeInMinutes: Int?,

    /** How many minutes on the lower end it takes to complete the thing/game. */
    @JacksonXmlProperty(localName = "minplaytime")
    @JsonDeserialize(using = WrappedIntDeserializer::class)
    val minPlayingTimeInMinutes: Int?,

    /** How many minutes on the high end it takes to complete the thing/game. */
    @JacksonXmlProperty(localName = "maxplaytime")
    @JsonDeserialize(using = WrappedIntDeserializer::class)
    val maxPlayingTimeInMinutes: Int?,

    /** Minimum age to play/participate in the thing. */
    @JsonDeserialize(using = WrappedIntDeserializer::class) val minAge: Int?,

    /** A list of videos associated with the thing, could be reviews, how to plays, unboxing etc. */
    @JacksonXmlElementWrapper(localName = "videos") val videos: List<Video> = listOf(),

    /**
     * Comments associated with the thing, these could be either ratings (including a comment) or a
     * comment without a rating value.
     */
    val comments: Comments?,

    /** Ratings/Statistics for the thing. */
    val statistics: Statistics?,

    /** Marketplace data. */
    @JacksonXmlElementWrapper(useWrapping = true)
    @JacksonXmlProperty(localName = "marketplacelistings")
    val listings: List<MarketplaceListing> = listOf(),

    /** Collection of different versions e.g. prints. */
    @JacksonXmlElementWrapper(useWrapping = true)
    @JacksonXmlProperty(localName = "versions")
    val versions: List<Version> = listOf(),

    /** Series codes for RPG items. */
    @JsonDeserialize(using = WrappedStringDeserializer::class) val seriesCode: String?,

    /** The issue index for the RPG issue. */
    @JsonDeserialize(using = WrappedIntDeserializer::class) val issueIndex: Int?,

    /** Primary name. */
    @JsonIgnore var name: String = "",

    /** Contains a list of polls such as the [PlayerAgePoll]. */
    var polls: List<Poll> = listOf(),

    /** Contains a list of poll summaries that relate to the `polls`. */
    @JacksonXmlProperty(localName = "poll-summary") var pollSummary: List<PollSummary> = listOf(),

    /**
     * Depending on the [type] this list may contain different links e.g. for boardgames links such
     * as: `boardgamecategory`, `boardgamefamily`, `boardgamemechanic` etc. may be included. For
     * `rpgitem` similar but different links are returned e.g. `rpgitemcategory` etc.
     */
    var links: List<Link> = listOf(),

    /** Names of the thing, consisting of a primary and optionally alternatives. */
    var names: List<Name> = listOf(),
) {
    @JsonSetter("poll")
    fun internalSetPolls(value: List<Poll>) {
        polls = polls + value
    }

    @JsonSetter("link")
    fun internalSetLinks(value: List<Link>) {
        links = links + value
    }

    @JsonSetter("name")
    fun internalSetNames(value: List<Name>) {
        names = names + value
        names.forEach { if (it.type == "primary") name = it.value }
    }
}

/** Available versions of the thing e.g. Different prints of a boardgame. */
@Serializable
data class Version(
    /** Type of version e.g. 'boardgameversion'. */
    @JacksonXmlProperty(isAttribute = true) val type: String,

    /** Unique ID of this product. */
    @JacksonXmlProperty(isAttribute = true) val id: Int,

    /** Thumbnail image of the product - 200x150. */
    val thumbnail: String?,

    /** Full suze image of the product. */
    val image: String?,

    /** When the product was published. */
    @JsonDeserialize(using = WrappedIntDeserializer::class) val yearPublished: Int?,

    /** The year it was released in e.g. `2019`. (For video games) */
    @JsonDeserialize(using = WrappedLocalDateDeserializer::class)
    @Serializable(with = LocalDateSerializer::class)
    val releaseDate: LocalDate?,

    /** Product code of the product. */
    @JsonDeserialize(using = WrappedStringDeserializer::class) val productCode: String?,

    /** Width in inches. */
    @JsonDeserialize(using = WrappedDoubleDeserializer::class) val width: Double?,

    /** Length in inches. */
    @JsonDeserialize(using = WrappedDoubleDeserializer::class) val length: Double?,

    /** Depth in inches. */
    @JsonDeserialize(using = WrappedDoubleDeserializer::class) val depth: Double?,

    /** Weight in lbs (pounds). */
    @JsonDeserialize(using = WrappedDoubleDeserializer::class) val weight: Double?,

    /** Primary name. */
    @JsonIgnore var name: String = "",

    /** Names of the product, consisting of a primary and optionally alternatives. */
    var names: List<Name> = listOf(),

    /** Additional information about this product e.g. Language, artist(s) etc. */
    var links: List<Link> = listOf(),
) {
    @JsonSetter("name")
    fun internalSetNames(value: List<Name>) {
        names = names + value
        names.forEach { if (it.type == "primary") name = it.value }
    }

    /** Additional information about this product e.g. Language, artist(s) etc. */
    @JsonSetter("link")
    fun internalSetLinks(value: List<Link>) {
        links = links + value
    }
}

/**
 * Describes an associated video on the thing. In addition to a title and link the videos are also
 * categorized: `instructional`, `review` etc. and contain poster(user) information.
 */
@Serializable
data class Video(
    /** The unique ID to retrieve the video on BGG. */
    @JacksonXmlProperty(isAttribute = true) val id: Int,

    /** Title of the video */
    @JacksonXmlProperty(isAttribute = true) val title: String,

    /**
     * Category explaining roughly its contents, category should be on of the following:
     * * Review
     * * Session
     * * Instructional
     * * Interview
     * * Unboxing
     * * Humor
     * * Other
     */
    @JacksonXmlProperty(isAttribute = true) val category: String,

    /** English name of language that the video is presented it e.g. `English`, `Spanish` etc. */
    @JacksonXmlProperty(isAttribute = true) val language: String,

    /** A URL to either a Youtube or Vimeo video. */
    @JacksonXmlProperty(isAttribute = true) val link: String,

    /** The username of the user that posted the video - not necessarily the video's author. */
    @JacksonXmlProperty(isAttribute = true) val username: String,

    /** The id of the user that posted the video - not necessarily the video's author. */
    @JacksonXmlProperty(isAttribute = true) val userid: Int,

    /** When the video was posted. */
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssz")
    @Serializable(with = LocalDateTimeSerializer::class)
    val postDate: LocalDateTime,
)

// region Comments
/**
 * A collection of [Comment] objects including pagination date. NOTE that this object is reused for
 * both `comments` and `ratingcomments`
 *
 * @see org.audux.bgg.request.things
 */
@Serializable
data class Comments(
    /**
     * The current page of comments.
     *
     * @see org.audux.bgg.request.things
     */
    @JacksonXmlProperty(isAttribute = true) val page: Int,

    /**
     * Total number of comments for the specified type of comments, current items in this collection
     * are specified by the request: [org.audux.bgg.common.Constants.PARAM_PAGE] and
     * [org.audux.bgg.common.Constants.PARAM_PAGE_SIZE] which are passed in via the `page` and
     * `pageSize` parameters in [org.audux.bgg.request.things].
     */
    @JacksonXmlProperty(isAttribute = true) val totalItems: Int,

    /**
     * List of comments which will either always have `rating` set or value set depending on the
     * request.
     */
    @JacksonXmlProperty(localName = "comment") val comments: List<Comment> = listOf(),
)

/**
 * Encapsulated both ratings and comments. Comments can have both [rating] and [value] set but which
 * one will always be set in the collection is specified in the request instead.
 */
@Serializable
data class Comment(
    /** Username of the user that posted the rating/comment. */
    @JacksonXmlProperty(isAttribute = true) val username: String,

    /**
     * A rating expressed in a number ranging from 1-10. May be expressed as a decimal number or
     * "N/A".
     */
    @JacksonXmlProperty(isAttribute = true) val rating: String?,

    /** The comment the user posted. */
    @JacksonXmlProperty(isAttribute = true) val value: String?,
)

// endregion

// region Marketplace
/** A single listing for the thing i.e. a 'for sale'-listing. */
@Serializable
data class MarketplaceListing(
    /** When the listing was created. */
    @JsonDeserialize(using = WrappedLocalDateTimeDeserializer::class)
    @Serializable(with = LocalDateTimeSerializer::class)
    val listDate: LocalDateTime,

    /** The requested price for the listing. */
    val price: Price,

    /** The condition of the item e.g. 'new' etc. */
    @JsonDeserialize(using = WrappedStringDeserializer::class) val condition: String?,

    /** Description of the listing. */
    @JsonDeserialize(using = WrappedStringDeserializer::class) val notes: String?,

    /** Link to the listing. */
    @JsonProperty("link") val webLink: Weblink,
)

/** Encapsulates a price for a given [MarketplaceListing] */
@Serializable
data class Price(
    /** The actual price. */
    val value: Double,

    /** The currency for the listing - ISO 4217. */
    val currency: String,
)

/** Link to a web resource including a title/description. */
@Serializable
data class Weblink(
    /** Link to web resource. */
    @Serializable(with = URISerializer::class) val href: URI,

    /** The title of the resource. */
    val title: String,
)

// endregion

// region All Poll classes
/**
 * Base type for creating the different polls in the response e.g. player poll/votes for the best
 * number of players to play a particular game with.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.EXISTING_PROPERTY,
    property = "name",
)
@JsonSubTypes(
    JsonSubTypes.Type(value = LanguageDependencePoll::class, name = "language_dependence"),
    JsonSubTypes.Type(value = PlayerAgePoll::class, name = "suggested_playerage"),
    JsonSubTypes.Type(value = NumberOfPlayersPoll::class, name = "suggested_numplayers"),
)
@Serializable(with = PollSerializer::class)
sealed interface Poll

/** Poll that contains the votes for the preferred number of players to engage with the thing. */
@Serializable
data class NumberOfPlayersPoll(
    /** English name/title of the poll e.g. "User Suggested Number of Players". */
    @JacksonXmlProperty(isAttribute = true) val title: String,

    /** Total number of votes cast. */
    @JacksonXmlProperty(isAttribute = true) val totalVotes: Int,

    /** Result set for the poll. */
    val results: List<NumberOfPlayersResults>,
) : Poll

/**
 * Suggested number of players for associated thing. These results are 2-dimensional meaning each
 * 'results' contains three singular results.
 *
 * The XML looks as follows (Repeated for each `numplayer` possibility):
 * ```
 *  <results numplayers="1">
 *      <result value="Best" numvotes="0"/>
 *      <result value="Recommended" numvotes="0"/>
 *      <result value="Not Recommended" numvotes="2"/>
 *  </results>
 * ```
 */
@Serializable
data class NumberOfPlayersResults(
    /** The number of players these votes were cast for. */
    @JacksonXmlProperty(localName = "numplayers") val numberOfPlayers: String,

    /** The list of results/votes. */
    @JacksonXmlProperty(localName = "result") val results: List<PollResult>,
)

/** Poll that contains the votes for the minimum age to engage with the thing. */
@Serializable
data class PlayerAgePoll(
    /** English name/title of the poll e.g. "User Suggested Player Age". */
    @JacksonXmlProperty(isAttribute = true) val title: String,

    /** Total number of votes cast. */
    @JacksonXmlProperty(isAttribute = true) val totalVotes: Int,

    /** Result set for the poll. */
    @JacksonXmlElementWrapper(useWrapping = true)
    @JacksonXmlProperty(localName = "results")
    val results: List<PollResult>,
) : Poll

/** Poll that contains the votes for the minimum age to engage with the thing. */
@Serializable
data class LanguageDependencePoll(
    /** English name/title of the poll e.g. "Language Dependence". */
    @JacksonXmlProperty(isAttribute = true) val title: String,

    /** Total number of votes cast. */
    @JacksonXmlProperty(isAttribute = true) val totalVotes: Int,

    /** Result set for the poll. */
    @JacksonXmlElementWrapper(useWrapping = true)
    @JacksonXmlProperty(localName = "results")
    val results: List<LeveledPollResult>,
) : Poll

/** A single aggregate result i.e. votes for a single poll option. */
@Serializable
data class PollResult(
    /** The name/value of the poll option. */
    @JacksonXmlProperty(isAttribute = true) val value: String,

    /** The number of votes cast on this option. */
    @JacksonXmlProperty(localName = "numvotes", isAttribute = true) val numberOfVotes: Int,
)

/** A single aggregate result i.e. votes for a single poll option including a level/gradation. */
@Serializable
data class LeveledPollResult(
    /** The name/value of the poll option. */
    @JacksonXmlProperty(isAttribute = true) val value: String,

    /** The number of votes cast on this option. */
    @JacksonXmlProperty(localName = "numvotes", isAttribute = true) val numberOfVotes: Int,

    /** The level or gradation of his option e.g. 1 with other options going up to 5. */
    @JacksonXmlProperty(isAttribute = true) val level: Int,
)

/**
 * Small version of the [Poll] result objects that only return the top results like 'bestwith' or
 * 'recommendedwith'.
 */
@Serializable
data class PollSummary(
    /** The name of the poll. */
    @JacksonXmlProperty(isAttribute = true) val name: String?,

    /** The title of the poll. */
    @JacksonXmlProperty(isAttribute = true) val title: String?,

    /** Result set for the poll summary. */
    @JacksonXmlElementWrapper(useWrapping = false)
    @JacksonXmlProperty(localName = "result")
    val results: List<PollSummaryResult> = emptyList(),
)

/** A result for the summaries like: "bestwith". */
@Serializable
data class PollSummaryResult(
    /** The name of the poll option. */
    @JacksonXmlProperty(isAttribute = true) val name: String?,

    /** The title of the poll option. */
    @JacksonXmlProperty(isAttribute = true) val value: String?,
)
// endregion Polls
