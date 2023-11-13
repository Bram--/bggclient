package org.audux.bgg.data.response

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import org.audux.bgg.data.request.things

@JsonRootName("items")
data class Things(
    /** List of things. */
    @JsonProperty("item")
    val things: List<Thing>
)

data class Thing(
    /** Unique BGG identifier. */
    val id: Int,

    /**
     * The type of thing e.g. boardgame, expansion etc.
     *
     *  @see org.audux.bgg.BggClient.things
     */
    val type: String,

    /** URL to 200 by 150 thumbnail image.  */
    val thumbnail: String?,

    /** URL to full sized image.  */
    val image: String?,

    /** Names of the thing, consisting of a primary and optionally alternatives. */
    val name: Name,

    /** Long form description of the thing. */
    val description: String?,

    /** The year it was published in e.g. `2019`. */
    val yearPublished: Int,

    /** Minimum number of players required. */
    val minPlayers: Int?,

    /** Maximum number of players. */
    val maxPlayers: Int?,

    /** How many minutes on average it takes to complete the thing/game. */
    val playingTimeInMinutes: Int,

    /** How many minutes on the lower end it takes to complete the thing/game. */
    val minPlayingTimeInMinutes: Int,

    /** How many minutes on the high end it takes to complete the thing/game. */
    val maxPlayingTimeInMinutes: Int,

    /** Minimum age to play/participate in thhe thing. */
    val minAge: Int,

    /**  */
    val link: List<Link> = listOf(),
    val videos: Videos?,
    val comments: Comments?,
)

data class Name(
    val value: String,
    val type: String,
    val sortIndex: Int
)

data class Link(
    val id: Int,
    val type: String,
    val value: String
)

data class Videos(
    val total: Int,
    val video: List<Video> = listOf(),
)

data class Video(
    val id: Int,
    val title: String,
    val category: String,
    val language: String,
    val link: String,
    val username: String,
    val userid: Int,
    // TODO: Change to DateTime.
    val postdate: String
)

data class Comments(
    val page: Int,
    val totalItems: Int,
    val comment: List<Comment> = listOf(),
)

data class Comment(
    val username: String,
    val rating: Int?,
    val value: String,
)