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
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlText
import org.audux.bgg.request.Constants
import java.time.LocalDateTime

/** Response wrapper for Hot lists to be returned. */
@JsonRootName("geeklist")
@JsonIgnoreProperties("postdate_timestamp", "editdate_timestamp")
data class GeekList(
    /** Terms of use of the BGG API. */
    @JacksonXmlProperty(isAttribute = true) val termsOfUse: String,

    /** Unique ID for the Geek list - same as in the request. */
    @JacksonXmlProperty(isAttribute = true) val id: Number,
    @JsonFormat(pattern = Constants.REQUEST_DATE_TIME_FORMAT) val postDate: LocalDateTime,
    @JsonFormat(pattern = Constants.REQUEST_DATE_TIME_FORMAT) val editDate: LocalDateTime,
    val thumbs: Number,
    val numItem: Number,
    val username: String,
    val title: String,
    val description: String,

    /** List of the actual items in the list. */
    @JacksonXmlProperty(localName = "comment") val comments: List<GeekListComment>,

    /** List of the actual items in the list. */
    @JacksonXmlProperty(localName = "item") val results: List<GeekListItem>
)

data class GeekListComment(
    @JacksonXmlProperty(isAttribute = true) val username: String,
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(pattern = Constants.REQUEST_DATE_TIME_FORMAT)
    val date: LocalDateTime,
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(pattern = Constants.REQUEST_DATE_TIME_FORMAT)
    val postDate: LocalDateTime,
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(pattern = Constants.REQUEST_DATE_TIME_FORMAT)
    val editDate: LocalDateTime,
    @JacksonXmlProperty(isAttribute = true) val thumbs: Number,
) {
    /** The comment the user posted. */
    // As this is the innerText of the XML element, using this in a (Kotlin) data class does
    // currently not work. See: https://github.com/FasterXML/jackson-module-kotlin/issues/138
    @JacksonXmlText var value: String? = null
}

// <item id="186612" objecttype="thing" subtype="boardgame" objectid="822" objectname="Carcassonne"
// username="zefquaavius" postdate="Thu, 10 Nov 2005 02:46:36 +0000" editdate="Mon, 05 Nov 2007
// 19:57:31 +0000" thumbs="2" imageid="100654">
// <body>2000 [b]Carcassonne[/b] 72 land tiles (1 of which is a starting tile with a darker back) 8
// followers × 5 colors (blue, yellow, green, red, black) 1 scoring track</body>
// </item>
data class GeekListItem(
    @JacksonXmlProperty(isAttribute = true) val id: Int,
    @JacksonXmlProperty(isAttribute = true) val objectType: String,
    @JacksonXmlProperty(isAttribute = true) val subType: org.audux.bgg.common.SubType,
    @JacksonXmlProperty(isAttribute = true) val objectName: String,
    @JacksonXmlProperty(isAttribute = true) val objectId: Int,
    @JacksonXmlProperty(isAttribute = true) val username: String,
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(pattern = Constants.REQUEST_DATE_TIME_FORMAT)
    val postDate: LocalDateTime,
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(pattern = Constants.REQUEST_DATE_TIME_FORMAT)
    val editDate: LocalDateTime,
    @JacksonXmlProperty(isAttribute = true) val thumbs: Number,
    @JacksonXmlProperty(isAttribute = true) val imageId: Number? = null,
    @JsonDeserialize(using = TrimmedStringDeserializer::class) val body: String,
)
