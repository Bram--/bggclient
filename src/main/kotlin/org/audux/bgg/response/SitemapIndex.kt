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
import kotlinx.serialization.Serializable
import org.audux.bgg.common.SitemapLocationType

/**
 * The sitemap index contains all Sitemaps. A sitemap might be broken up in several `pages`. For
 * example the board game sitemap is broken up in 16 sitemaps or pages.
 */
@JsonRootName("sitemapindex")
@JsonIgnoreProperties("schemaLocation")
@Serializable
data class SitemapIndex(
    /** List of sitemap locations. */
    @JacksonXmlProperty(localName = "sitemap") val sitemaps: List<SitemapLocation>
)

/** A URL/Location of a single sitemap/sitemap page. */
@Serializable
data class SitemapLocation(
    /** The URL/Web address of the sitemap. */
    @JsonProperty("loc")
    @JsonDeserialize(using = TrimmedStringDeserializer::class)
    val location: String
) {
    /**
     * Calculated property that represents the type of sitemap e.g. Board games, Video games, Board
     * game expansions etc.
     */
    val type: SitemapLocationType = SitemapLocationType.fromURL(location)
}
