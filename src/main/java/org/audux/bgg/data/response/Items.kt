package org.audux.bgg.data.response

data class Items(
    val item: List<Item>
)
data class Item(
    val id: Int,
    val type: String,
    val thumbnail: String,
    val image: String,
    val name: Name,
    val description: String,
    val yearPublished: Int,
    val minPlayers: Int,
    val maxPlayers: Int,
    val playingTimeInMinutes: Int,
    val minPlayingTimeInMinutes: Int,
    val maxPlayingTimeInMinutes: Int,
    val minAge: Int,
    val link: List<Link>,
    val videos: Videos,
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
    val video: List<Video>,
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