package org.audux.bgg.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import kotlinx.datetime.LocalDateTime
import java.net.URI

/** Response wrapper for the things to be returned. */
@JsonRootName("items")
data class Things(
    /** List of the actual things. */
    @JsonProperty("item")
    val things: List<Thing>
)

/**
 * An item or thing which could be either of the [org.audux.bgg.data.request.ThingType] objects. As
 * a result of the loose overlap of the types most values are Nullable, however type specific data
 * is supplied via the [link] property.
 *
 * Furthermore, the settings/filling of the properties is highly dependent on the request. This is
 * because additional parameters need to be set in order for the data to be retrieved. For example
 * making a things request with simply an `id` parameter will not return any videos, comments,
 * marketplace data, statistics or version information.
 *
 * @see org.audux.bgg.data.request.things
 */
data class Thing(
    /** Unique BGG identifier. */
    val id: Int,

    /**
     * The type of thing e.g. boardgame, expansion etc.
     *
     *  @see org.audux.bgg.data.request.ThingType
     */
    val type: String,

    /** URL to 200 by 150 thumbnail image.  */
    val thumbnail: String?,

    /** URL to full sized image.  */
    val image: String?,

    /** Names of the thing, consisting of a primary and optionally alternatives. */
    val name: List<Name>,

    /** Long form description of the thing. */
    val description: String?,

    /** The year it was published in e.g. `2019`. */
    val yearPublished: WrappedValue<Int>?,

    /** Minimum number of players required. */
    val minPlayers: WrappedValue<Int>?,

    /** Maximum number of players. */
    val maxPlayers: WrappedValue<Int>?,

    /** How many minutes on average it takes to complete the thing/game. */
    val playingTimeInMinutes: WrappedValue<Int>?,

    /** How many minutes on the lower end it takes to complete the thing/game. */
    val minPlayingTimeInMinutes: WrappedValue<Int>?,

    /** How many minutes on the high end it takes to complete the thing/game. */
    val maxPlayingTimeInMinutes: WrappedValue<Int>?,

    /** Minimum age to play/participate in thhe thing. */
    val minAge: WrappedValue<Int>?,

    /**
     * Depending on the [type] this list may contain different links e.g. for boardgames links such
     * as: `boardgamecategory`, `boardgamefamily`, `boardgamemechanic` etc. may be included. For
     * `rpgitem` similar but different links are returned e.g. `rpgitemcategory` etc.
     */
    val link: List<Link> = listOf(),

    /**
     * A list of videos associated with the thing, could be reviews, how to plays, unboxing etc.
     */
    @JacksonXmlElementWrapper(localName = "videos")
    val videos: List<Video> = listOf(),

    /**
     * Comments associated with the thing, these could be either ratings (including a comment) or
     * a comment without a rating value.
     */
    val comments: Comments?,

    /** Ratings/Statistics for the thing. */
    val statistics: Statistics?,

    @JacksonXmlElementWrapper(useWrapping = true)
    @JacksonXmlProperty(localName = "marketplacelistings", isAttribute = false)
    val listings: List<MarketplaceListing> = listOf(),
)

/** Encapsulates the name of a Thing either primary or alternate name. */
data class Name(
    /** The actual name. */
    @JacksonXmlProperty(isAttribute = true)
    val value: String,

    /** The type either: `primary` or `alternate`. */
    @JacksonXmlProperty(isAttribute = true)
    val type: String,

    /**
     * The order the names are displayed on the website. NOTE that primary and alternate might
     * have overlapping indexes.
     */
    @JacksonXmlProperty(isAttribute = true)
    val sortIndex: Int
)

/**
 * Describes a link or relationship to another class of object. For example a board game thing may
 * contain a list of links to a `boardgamemechanic` like `Income`, `Hand management`.
 * Common types are:
 *
 *   * boardgameartist
 *   * boardgamecategory
 *   * boardgamedesigner
 *   * boardgameexpansion
 *   * boardgamemechanic
 *   * rpgitemartist
 *   * rpgitemcategory
 *   * rpgitemdesigner
 *   * rpgitemexpansion
 *   * rpgitemmechanic
 *
 *   And so on.
 */
data class Link(
    /**
     * The id for the link, most of these cannot be retrieved via the API although a
     * 'family'-API exists.
     */
    @JacksonXmlProperty(isAttribute = true)
    val id: Int,

    /** The unique name of the Link i.e. links with the same ID will always carry the same name. */
    @JacksonXmlProperty(isAttribute = true)
    val value: String,

    /**
     * The type of the link as outlined in the class description.
     */
    @JacksonXmlProperty(isAttribute = true)
    val type: String,
)

/**
 * Describes an associated video on the thing.
 * In addition to a title and link the videos are also categorized: `instructional`, `review` etc.
 * and contain poster(user) information.
 */
data class Video(
    /** The unique ID to retrieve the video on BGG. */
    @JacksonXmlProperty(isAttribute = true)
    val id: Int,

    /** Title of the video */
    @JacksonXmlProperty(isAttribute = true)
    val title: String,

    /**
     * Category explaining roughly its contents, category should be on of the following:
     *
     *  * Review
     *  * Session
     *  * Instructional
     *  * Interview
     *  * Unboxing
     *  * Humor
     *  * Other
     */
    @JacksonXmlProperty(isAttribute = true)
    val category: String,

    /** English name of language that the video is presented it e.g. `English`, `Spanish` etc. */
    @JacksonXmlProperty(isAttribute = true)
    val language: String,

    /** A URL to either a Youtube or Vimeo video. */
    @JacksonXmlProperty(isAttribute = true)
    val link: String,

    /** The username of the user that posted the video - not necessarily the video's author. */
    @JacksonXmlProperty(isAttribute = true)
    val username: String,

    /** The id of the  user that posted the video - not necessarily the video's author. */
    @JacksonXmlProperty(isAttribute = true)
    val userid: Int,

    /** When the video was posted. */
    // TODO: Change to DateTime.
    @JacksonXmlProperty(isAttribute = true)
    val postdate: String
)

/**
 * A collection of [Comment] objects including pagination date.
 * NOTE that this object is reused for both `comments` and `ratingcomments`
 *
 * @see org.audux.bgg.data.request.things
 */
data class Comments(
    /**
     * The current page of comments.
     *
     * @see org.audux.bgg.data.request.things
     */
    @JacksonXmlProperty(isAttribute = true)
    val page: Int,

    /**
     * Total number of comments for the specified type of comments, current items in this collection
     * are specified by the request:
     * [org.audux.bgg.BggClient.PARAM_PAGE] and [org.audux.bgg.BggClient.PARAM_PAGE_SIZE] which are
     * passed in via the `page` and `pageSize` parameters in [org.audux.bgg.data.request.things].
     */
    @JacksonXmlProperty(isAttribute = true)
    val totalItems: Int,

    /**
     * List of comments which will either always have `rating` set or value set depending on
     * the request.
     */
    val comment: List<Comment> = listOf(),
)

/**
 * Encapsulated both ratings and comments. Comments can have both [rating] and [value] set but
 * which one will always be set in the collection is specified in the request instead.
 */
data class Comment(
    /** Username of the user that posted the rating/comment. */
    @JacksonXmlProperty(isAttribute = true)
    val username: String,

    /** A rating expressed in a number ranging from 1-10. May be expressed as a decimal number. */
    @JacksonXmlProperty(isAttribute = true)
    val rating: Number?,

    /** The comment the user posted. */
    @JacksonXmlProperty(isAttribute = true)
    val value: String?,
)

/** Wrapper for [Ratings]. */
data class Statistics(
    val ratings: Ratings,
)

/**
 * Contains rating aggregated and other statistics like average rating,
 * standard deviation, number of comments.
 */
data class Ratings(
    /** Number of user ratings. */
    val usersRated: WrappedValue<Number>?,

    /** The average rating. */
    val average: WrappedValue<Number>?,

    /** Standard deviation. */
    val stdDev: WrappedValue<Number>?,

    /** Bayesian average rating. */
    val bayesAverage: WrappedValue<Number>?,

    /** The median rating. */
    val median: WrappedValue<Number>?,

    /** Total number of users owning this thing. */
    val owned: WrappedValue<Number>?,

    /** Total number of users looking to trade away this thing. */
    val trading: WrappedValue<Number>?,

    /** Total number of users wanting this thing. */
    val wanting: WrappedValue<Number>?,

    /** Total number of users wishing for this thing. */
    val wishing: WrappedValue<Number>?,

    /** Total number of comments left on the thing. */
    val numComments: WrappedValue<Number>?,

    /** Number of weight ratings. */
    val numWeights: WrappedValue<Number>?,

    /** Average weight rating. */
    val averageWeight: WrappedValue<Number>?,

    /**
     * A thing can be listed on different rankings. For example a board game could both be ranked as
     * a board game and a strategy game.
     */
    @JacksonXmlElementWrapper(localName = "ranks")
    val ranks: List<Rank> = listOf(),
)

/** Represents a rank in a single ranking (Consisting of type & name). */
data class Rank(
    /** Unique of the ranking type - ID and type+name should always be a coupled. */
    @JacksonXmlProperty(isAttribute = true)
    val id: Int,

    /** Type of ranking e.g. the thing's main type or sub type*/
    @JacksonXmlProperty(isAttribute = true)
    val type: String?,

    /** The name of the ranking e.g. "boardgame", "strategygame" etc. */
    @JacksonXmlProperty(isAttribute = true)
    val name: String,

    /** Friendly/Natural language name of the ranking. */
    @JacksonXmlProperty(isAttribute = true)
    val friendlyName: String,

    /** The actual rank of the thing in this ranking. */
    @JacksonXmlProperty(isAttribute = true)
    val value: Number?,

    /** It's bayesian average in this ranking. */
    @JacksonXmlProperty(isAttribute = true)
    val bayesAverage: Number?,
)

data class MarketplaceListing(
    /** When the listing was created. */
    // TODO: Parse Datetime.
    val listDate: WrappedValue<LocalDateTime>,

    /** The requested price for the listing. */
    val price: Price,

    /** The condition of the item e.g. 'new' etc. */
    val condition: WrappedValue<String>?,

    /** Description of the listing. */
    val notes: WrappedValue<String>?,

    /** Link to the listing. */
    val link: Weblink,
)

/** Encapsulates a price for a given [MarketplaceListing] */
data class Price(
    /** The actual price. */
    val value: Number,

    /** The currency for the listing - ISO 4217. */
    val currency: String,
)

/** Link to a web resource including a title/description. */
data class Weblink(
    /** Link to web resource. */
    val href: URI,

    /** The title of the resource. */
    val title: String,
)

/** 'Hack' as many BGG API values are stored in an attribute e.g. '<element value='2.123 />'' */
data class WrappedValue<T>(
    val value: T,
)