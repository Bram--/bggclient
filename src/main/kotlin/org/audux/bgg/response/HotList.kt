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

import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty

/** Response wrapper for Hot lists to be returned. */
@JsonRootName("items")
data class HotList(
    /** Terms of use of the BGG API. */
    @JacksonXmlProperty(isAttribute = true) val termsOfUse: String,

    /** List of the actual things. */
    @JacksonXmlProperty(localName = "item") val results: List<HotListItem>
)

/** Encapsulates a ranked item in the hot list. */
data class HotListItem(
    /** Unique ID that can be used to look up more information using the thing endpoint. */
    @JacksonXmlProperty(isAttribute = true) val id: Int,

    /** Rank in the returned list. */
    @JacksonXmlProperty(isAttribute = true) val rank: Int,

    /** Primary name represented as [WrappedValue]. */
    @JsonDeserialize(using = WrappedStringDeserializer::class) val name: String?,

    /** Thumbnail image URL of the item. */
    @JsonDeserialize(using = WrappedStringDeserializer::class) val thumbnail: String?,

    /** Optional year of publishing. */
    @JsonDeserialize(using = WrappedIntDeserializer::class) val yearPublished: Int?,
)
