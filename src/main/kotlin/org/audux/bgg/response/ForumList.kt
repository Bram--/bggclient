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
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDateTime
import kotlinx.serialization.Serializable
import org.audux.bgg.common.ForumListType

/** Response wrapper for a list of forums for the given id/thing pair. */
@JsonRootName("forums")
@Serializable
data class ForumList(
    /** Terms of use of the BGG API. */
    @JacksonXmlProperty(isAttribute = true) val termsOfUse: String,

    /** Unique ID of the thing/family, same as the request. */
    @JacksonXmlProperty(isAttribute = true) val id: Int,

    /** The type of the returned thing/family, same as the request. */
    @JsonDeserialize(using = ForumListTypeDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val type: ForumListType,

    /** List of the actual available forums. */
    @JacksonXmlProperty(localName = "forum") val forums: List<ForumSummary>
)

/**
 * Encapsulates the summary of a forum - these can be retrieve using the
 * [org.audux.bgg.request.forum] endpoint.
 */
@Serializable
data class ForumSummary(
    /** Unique ID that can be used to look up more information using the forum endpoint. */
    @JacksonXmlProperty(isAttribute = true) val id: Int,

    /** TODO: document. */
    @JacksonXmlProperty(isAttribute = true) val groupId: Int,

    /** Whether posting is allowed or not. */
    @JsonDeserialize(using = NumberToBooleanDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val noPosting: Boolean,

    /** Title/Name of the forum. */
    @JsonDeserialize(using = TrimmedStringDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val title: String,

    /** A more detailed description of the forum. */
    @JsonDeserialize(using = TrimmedStringDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val description: String,

    /** The number of threads that are active/created in the forum. */
    @JacksonXmlProperty(isAttribute = true) val numThreads: Int,

    /** Total number of posts in the forum spread over the threads. */
    @JacksonXmlProperty(isAttribute = true) val numPosts: Int,

    /** The date and time a post was last made. */
    @JsonFormat(pattern = "E, dd MMM yyyy HH:mm:ss Z")
    @JacksonXmlProperty(isAttribute = true)
    @Serializable(with = LocalDateSerializer::class)
    val lastPostDate: LocalDateTime?,
)
