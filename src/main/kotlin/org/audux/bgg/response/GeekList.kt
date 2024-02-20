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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import java.time.LocalDateTime
import org.audux.bgg.request.Constants

/** Encapsulates a geek list including its items and optionally its comments. */
@JsonRootName("geeklist")
@JsonIgnoreProperties("postdate_timestamp", "editdate_timestamp")
data class GeekList(
    /** Terms of use of the BGG API. */
    @JacksonXmlProperty(isAttribute = true) val termsOfUse: String,

    /** Unique ID for the Geek list - same as in the request. */
    @JacksonXmlProperty(isAttribute = true) val id: Int,

    /** The date and time the geek list was posted/published. */
    @JsonFormat(pattern = Constants.REQUEST_XML1_DATE_TIME_FORMAT) val postDate: LocalDateTime,

    /** The date and time the geek list was last edited - the same as [postDate] if not edited. */
    @JsonFormat(pattern = Constants.REQUEST_XML1_DATE_TIME_FORMAT) val editDate: LocalDateTime,

    /** The number of thumbs up/likes the geek list received. */
    val thumbs: Int,

    /** The of number of items in the geek list. */
    val numItems: Int,

    /** The username of the user that published this geek list. */
    val username: String,

    /** The title of the geek list. */
    val title: String,

    /** A more detailed description of the geek list. */
    val description: String,

    /** The actual items in the geek list e.g. a list of board games. */
    @JacksonXmlProperty(localName = "item") val items: List<GeekListItem>
) {
    /** The list of comments of this list. */
    @JsonProperty("comment")
    var comments: List<GeekListComment> = mutableListOf()
        set(value) {
            field = field + value
        }
}

/** A single comment on either a Geek list or a Geek list item. */
data class GeekListComment(
    /** The username of the user that left the comment. */
    @JacksonXmlProperty(isAttribute = true) val username: String,

    /** The date the comment was originally posted. */
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(pattern = Constants.REQUEST_XML1_DATE_TIME_FORMAT)
    val date: LocalDateTime,

    /** The date the comment was originally posted. */
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(pattern = Constants.REQUEST_XML1_DATE_TIME_FORMAT)
    val postDate: LocalDateTime,

    /** The date the comment was last edited - the same as [postDate] if not edited. */
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(pattern = Constants.REQUEST_XML1_DATE_TIME_FORMAT)
    val editDate: LocalDateTime,

    /** The number of thumbs up/likes on this item. */
    @JacksonXmlProperty(isAttribute = true) val thumbs: Int,
) {
    /** The actual comment the user posted. */
    // As this is the innerText of the XML element, using this in a (Kotlin) data class does
    // currently not work. See: https://github.com/FasterXML/jackson-module-kotlin/issues/138
    @JsonDeserialize(using = TrimmedStringDeserializer::class)
    @JacksonXmlText
    var value: String? = null
}

/** An item in the geek list e.g. a board game including a description/[body] */
data class GeekListItem(
    /** The ID of the geek list item - NOT the id of the object. */
    @JacksonXmlProperty(isAttribute = true) val id: Int,

    /** The type of object e.g. 'thing' or family. */
    @JacksonXmlProperty(isAttribute = true) val objectType: String,

    /** The [org.audux.bgg.common.SubType] of the item e.g. a boardgame. */
    @JsonDeserialize(using = SubTypeDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val subType: org.audux.bgg.common.SubType,

    /** The Name of the item/object e.g. the name of the game. */
    @JacksonXmlProperty(isAttribute = true) val objectName: String,

    /**
     * The ID of the item/object which can be used in the [org.audux.bgg.request.familyItems] or
     * [org.audux.bgg.request.things] API.
     */
    @JacksonXmlProperty(isAttribute = true) val objectId: Int,

    /** The username of the user that added this item to the geek list. */
    @JacksonXmlProperty(isAttribute = true) val username: String,

    /** The original date this item was added/posted to the list. */
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(pattern = Constants.REQUEST_XML1_DATE_TIME_FORMAT)
    val postDate: LocalDateTime,

    /** The date this item was last edited/changed. */
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(pattern = Constants.REQUEST_XML1_DATE_TIME_FORMAT)
    val editDate: LocalDateTime,

    /** The number of thumbs up/likes this item has received. */
    @JacksonXmlProperty(isAttribute = true) val thumbs: Int,

    /**
     * The Image ID this item, these can be formatted as an URL as follows: `*
     * https://boardgamegeek.com/image/$imageId to display/look up the image.
     */
    @JacksonXmlProperty(isAttribute = true) val imageId: Int? = null,
    @JsonDeserialize(using = TrimmedStringDeserializer::class) val body: String,
) {
    /** The list of comments of this list. */
    @JsonProperty("comment")
    var comments: List<GeekListComment> = mutableListOf()
        set(value) {
            field = field + value
        }
}
