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
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDateTime
import org.audux.bgg.common.Ratings
import org.audux.bgg.common.ThingType

/** Response wrapper for Hot lists to be returned. */
@JsonRootName("items")
data class Collection(
    /** Terms of use of the BGG API. */
    @JacksonXmlProperty(isAttribute = true) val termsOfUse: String,

    /** Total number of items in the resulting collection. */
    @JacksonXmlProperty(isAttribute = true) val totalItems: Int,

    /** Date and time when the collection was 'published' i.e. created by the BGG API. */
    @JacksonXmlProperty(isAttribute = true, localName = "pubdate") val publishDate: String,

    /** List of the actual things. */
    @JacksonXmlProperty(localName = "item") val items: List<CollectionItem>
)

/** An item in the collection e.g. a board game, rpg etc. */
@JsonIgnoreProperties("objecttype")
data class CollectionItem(
    @JacksonXmlProperty(isAttribute = true, localName = "collid") val collectionId: Int,
    @JacksonXmlProperty(isAttribute = true) val objectId: Int,

    /** Rank in the returned list. */
    @JacksonXmlProperty(isAttribute = true, localName = "subtype")
    @JsonDeserialize(using = ThingTypeDeserializer::class)
    val type: ThingType,

    /** Name the user has given the thing or primary name of the thing by default. */
    val name: String,

    /** The primary name of the thing by default. */
    val originalName: String? = null,

    /** Optional year of publishing. */
    val yearPublished: Int? = null,

    /** Web URL to the thumbnail image. */
    @JsonDeserialize(using = TrimmedStringDeserializer::class) val thumbnail: String? = null,

    /** URL to the image. */
    @JsonDeserialize(using = TrimmedStringDeserializer::class) val image: String? = null,

    /** Status of the collection item e.g. whether the user owns it, previously owns it etc. */
    val status: Status? = null,

    /** Int of plays the user had with this item. */
    val numPlays: Int? = null,

    /** Comment owner/user left on the collection item. */
    val comment: String? = null,

    /** Text user left on the condition of the item. */
    val conditionText: String? = null,

    /** Aggregate statistics for this collection item. */
    val stats: CollectionStatistics?,
)

/** The status of collection item e.g. whether the user owns it, wants it etc. */
data class Status(
    /** Whether item is currently owned. */
    @JsonDeserialize(using = NumberToBooleanDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val own: Boolean = false,

    /** Whether item is currently owned. */
    @JsonDeserialize(using = NumberToBooleanDeserializer::class)
    @JacksonXmlProperty(isAttribute = true, localName = "prevowned")
    val previouslyOwned: Boolean = false,

    /** Whether item is currently owned. */
    @JsonDeserialize(using = NumberToBooleanDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val forTrade: Boolean = false,

    /** Whether item is currently owned. */
    @JsonDeserialize(using = NumberToBooleanDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val want: Boolean = false,

    /** Whether item is currently owned. */
    @JsonDeserialize(using = NumberToBooleanDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val wantToPlay: Boolean = false,

    /** Whether item is currently owned. */
    @JsonDeserialize(using = NumberToBooleanDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val wantToBuy: Boolean = false,

    /** Whether item is currently owned. */
    @JsonDeserialize(using = NumberToBooleanDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val wishlist: Boolean = false,

    /** Whether item is currently owned. */
    @JacksonXmlProperty(isAttribute = true) val wishlistPriority: Int? = null,

    /** Whether item is currently owned. */
    @JsonDeserialize(using = NumberToBooleanDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val preOrdered: Boolean = false,

    /** Whether item is currently owned. */
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    val lastModified: LocalDateTime? = null,
)

/** Statistics for a collection item. */
data class CollectionStatistics(
    /** Minimum number of players. */
    @JacksonXmlProperty(localName = "minplayers") val minimumPlayers: Int?,

    /** Maximum number of players. */
    @JacksonXmlProperty(localName = "maxplayers") val maximumPlayers: Int?,

    /** Minimum play time in minutes. */
    @JacksonXmlProperty(localName = "minplaytime") val minimumPlayTime: Int?,

    /** Maximum play time in minutes. */
    @JacksonXmlProperty(localName = "maxplaytime") val maximumPlayTime: Int?,

    /** Average playing time. */
    @JacksonXmlProperty(localName = "playingtime") val playingTime: Int?,

    /** How often this item is seen in collections. */
    val numOwned: Int?,

    /** The set of ratings */
    @JacksonXmlProperty(localName = "rating") val ratings: Ratings,
)
