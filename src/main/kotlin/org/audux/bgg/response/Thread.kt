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
import kotlinx.serialization.Serializable

/** Encapsulates an entire thread and all of its articles/posts. */
@JsonRootName("thread")
@Serializable
data class Thread(
    /** Terms of use of the BGG API. */
    @JacksonXmlProperty(isAttribute = true) val termsOfUse: String,

    /** Unique ID of this thread, same as in the request. */
    @JacksonXmlProperty(isAttribute = true) val id: Int,

    /** Link/URL to this thread on BGG. */
    @JacksonXmlProperty(isAttribute = true) val link: String,

    /** The title/subject of the thread. */
    val subject: String,

    /** The number of threads that are active/created in the forum. */
    @JacksonXmlProperty(isAttribute = true) val numArticles: Int,

    /** The list of articles in this thread. */
    @JacksonXmlElementWrapper(localName = "articles") val articles: List<Article> = listOf(),
)

/** Encapsulates an article in the current thread. */
@Serializable
data class Article(
    /** Unique ID that is used in the link below. */
    @JacksonXmlProperty(isAttribute = true) val id: Int,

    /** Link to the thread and this article specifically. */
    @JacksonXmlProperty(isAttribute = true) val link: String,

    /** Username of the article creator/poster. */
    @JsonDeserialize(using = TrimmedStringDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val username: String,

    /** The number of edits made to this article. */
    @JacksonXmlProperty(isAttribute = true) val numEdits: Int,

    /** The date and time this article was created. */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssz")
    @JacksonXmlProperty(isAttribute = true)
    @Serializable(with = LocalDateTimeSerializer::class)
    val postDate: LocalDateTime?,

    /** The date and time a post was last edited. */
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssz")
    @JacksonXmlProperty(isAttribute = true)
    @Serializable(with = LocalDateTimeSerializer::class)
    val editDate: LocalDateTime?,

    /** Title/Subject of the article/post. */
    @JsonDeserialize(using = TrimmedStringDeserializer::class) val subject: String,

    /** Body of the article/post. */
    @JsonDeserialize(using = TrimmedStringDeserializer::class) val body: String,
)
