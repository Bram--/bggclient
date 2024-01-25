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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDateTime

/** Encapsulates the summary of a forum. */
@JsonRootName("forum")
data class Forum(
    /** Terms of use of the BGG API. */
    @JacksonXmlProperty(isAttribute = true) val termsOfUse: String,

    /** Unique ID that can be used to look up more information using the forum endpoint. */
    @JacksonXmlProperty(isAttribute = true) val id: Int,

    /** Whether posting is allowed or not. */
    @JsonDeserialize(using = NumberToBooleanDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val noPosting: Boolean,

    /** Title/Name of the forum. */
    @JsonDeserialize(using = TrimmedStringDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val title: String,

    /** The number of threads that are active/created in the forum. */
    @JacksonXmlProperty(isAttribute = true) val numThreads: Number,

    /** Total number of posts in the forum spread over the threads. */
    @JacksonXmlProperty(isAttribute = true) val numPosts: Number,

    /** The date and time a post was last made. */
    @JsonFormat(pattern = "E, dd MMM yyyy HH:mm:ss Z")
    @JacksonXmlProperty(isAttribute = true)
    val lastPostDate: LocalDateTime?,

    /** The list of threads in this forum. */
    @JacksonXmlElementWrapper(localName = "threads") val threads: List<ThreadSummary> = listOf(),
)

/** Encapsulates a ranked item in the hot list. */
data class ThreadSummary(
    /** Unique ID that can be used to look up more information using the thread endpoint. */
    @JacksonXmlProperty(isAttribute = true) val id: Int,

    /** Title/Subject of the thread. */
    @JsonDeserialize(using = TrimmedStringDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val subject: String,

    /** Username of the thread creator. */
    @JsonDeserialize(using = TrimmedStringDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val author: String,

    /** The number of posts/articles in this thread. */
    @JacksonXmlProperty(isAttribute = true) val numArticles: Number,

    /** The date and time this thread was created. */
    @JsonFormat(pattern = "E, dd MMM yyyy HH:mm:ss Z")
    @JacksonXmlProperty(isAttribute = true)
    val postDate: LocalDateTime?,

    /** The date and time a post was last made in this thread. */
    @JsonFormat(pattern = "E, dd MMM yyyy HH:mm:ss Z")
    @JacksonXmlProperty(isAttribute = true)
    val lastPostDate: LocalDateTime?,
)
