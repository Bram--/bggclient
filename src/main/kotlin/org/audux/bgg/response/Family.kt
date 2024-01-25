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
import org.audux.bgg.common.FamilyType
import org.audux.bgg.common.Link
import org.audux.bgg.common.Name

/** Response wrapper for Family items, e.g. games having a related theme or gameplay. */
@JsonRootName("items")
data class Family(
    /** Terms of use of the BGG API. */
    @JacksonXmlProperty(isAttribute = true) val termsOfUse: String,

    /** List of the actual items. */
    @JacksonXmlProperty(localName = "item") val items: List<FamilyItem>
)

/** Encapsulates a ranked item in the hot list. */
data class FamilyItem(
    /** Unique ID that can be used to look up more information using the thing endpoint. */
    @JacksonXmlProperty(isAttribute = true) val id: Int,

    /** Type of family thing e.g. RPG, board game etc. */
    @JsonDeserialize(using = FamilyTypeDeserializer::class)
    @JacksonXmlProperty(isAttribute = true)
    val type: FamilyType,

    /** Primary name represented as [Name]. */
    val name: Name,

    /** Description of this family i.e. what relates the list of items. */
    @JsonDeserialize(using = TrimmedStringDeserializer::class) val description: String,

    /**
     * List of the items that are part of this family, e.g. board games that have a certain theme or
     * gameplay.
     */
    @JacksonXmlProperty(localName = "link") val links: List<Link>
)
