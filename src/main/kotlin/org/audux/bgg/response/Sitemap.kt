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

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDate
import kotlinx.serialization.Serializable

/** Represents a single Sitemap containing a set of URLs. */
@JsonRootName("urlset")
@JsonIgnoreProperties("schemaLocation")
@Serializable
data class Sitemap(
    /** List of the URLs in this Sitemap. */
    @JacksonXmlProperty(localName = "url") val sitemaps: List<SitemapUrl>
)

/**
 * Single URL/Location contained in the Sitemap; could, for example, represent a single Board game
 * page.
 */
@Serializable
data class SitemapUrl(
    /** The actual URL/Web address */
    @JsonProperty("loc")
    @JsonDeserialize(using = TrimmedStringDeserializer::class)
    val location: String,

    /** The frequency the page on the above address might be changed. */
    @JsonProperty("changefreq") val changeFrequency: String?,

    /** Priority the indexing crawler should consider this page as. */
    val priority: Double?,

    /** The date the page was last modified. */
    @JsonProperty("lastmod")
    @Serializable(with = LocalDateSerializer::class)
    val lastModified: LocalDate?,
)
