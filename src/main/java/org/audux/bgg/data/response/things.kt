package org.audux.bgg.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.annotation.JsonTypeInfo
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import org.audux.bgg.data.request.things

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
    val yearPublished: Int?,

    /** Minimum number of players required. */
    val minPlayers: Int?,

    /** Maximum number of players. */
    val maxPlayers: Int?,

    /** How many minutes on average it takes to complete the thing/game. */
    val playingTimeInMinutes: Int?,

    /** How many minutes on the lower end it takes to complete the thing/game. */
    val minPlayingTimeInMinutes: Int?,

    /** How many minutes on the high end it takes to complete the thing/game. */
    val maxPlayingTimeInMinutes: Int?,

    /** Minimum age to play/participate in thhe thing. */
    val minAge: Int?,

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
)

/** Encapsulates the name of a Thing either primary or alternate name. */
data class Name(
    /** The actual name. */
    val value: String,

    /** The type either: `primary` or `alternate`. */
    val type: String,

    /**
     * The order the names are displayed on the website. NOTE that primary and alternate might
     * have overlapping indexes.
     */
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
    val id: Int,

    /** The unique name of the Link i.e. links with the same ID will always carry the same name. */
    val value: String,

    /**
     * The type of the link as outlined in the class description.
     */
    val type: String,
)

/**
 * Describes an associated video on the thing.
 * In addition to a title and link the videos are also categorized: `instructional`, `review` etc.
 * and contain poster(user) information.
 */
data class Video(
    /** The unique ID to retrieve the video on BGG. */
    val id: Int,

    /** Title of the video */
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
    val category: String,

    /** English name of language that the video is presented it e.g. `English`, `Spanish` etc. */
    val language: String,

    /** A URL to either a Youtube or Vimeo video. */
    val link: String,

    /** The username of the user that posted the video - not necessarily the video's author. */
    val username: String,

    /** The id of the  user that posted the video - not necessarily the video's author. */
    val userid: Int,

    /** When the video was posted. */
    // TODO: Change to DateTime.
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
    val page: Int,

    /**
     * Total number of comments for the specified type of comments, current items in this collection
     * are specified by the request:
     * [org.audux.bgg.BggClient.PARAM_PAGE] and [org.audux.bgg.BggClient.PARAM_PAGE_SIZE] which are
     * passed in via the `page` and `pageSize` parameters in [org.audux.bgg.data.request.things].
     */
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
    val username: String,

    /** A rating expressed in a number ranging from 1-10. May be expressed as a decimal number. */
    val rating: Int?,

    /** The comment the user posted. */
    val value: String?,
)