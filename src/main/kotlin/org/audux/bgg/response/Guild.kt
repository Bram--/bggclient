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
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDate

/** Response wrapper for Hot lists to be returned. */
@JsonRootName("guild")
data class Guild(
    /** Terms of use of the BGG API. */
    @JacksonXmlProperty(isAttribute = true) val termsOfUse: String,
    @JacksonXmlProperty(isAttribute = true) val id: Number,
    @JacksonXmlProperty(isAttribute = true) val name: String,
    @JsonFormat(pattern = "E, dd MMM yyyy HH:mm:ss Z")
    @JacksonXmlProperty(isAttribute = true)
    val created: LocalDate,
    val category: String,
    val website: String,
    val manager: String,
    val description: String,
    val location: Location,
    val members: GuildMembers?,
)

data class Location(
    @JsonProperty("addr1") val addressLine1: String,
    @JsonProperty("addr2") val addressLine2: String,
    val city: String,
    val stateOrProvince: String,
    val postalCode: String,
    val country: String,
)

data class GuildMembers(
    @JacksonXmlProperty(isAttribute = true) val count: Number,
    @JacksonXmlProperty(isAttribute = true) val page: Number,
    @JsonProperty("member") val members: List<GuildMember>,
)

data class GuildMember(
    @JacksonXmlProperty(isAttribute = true) val name: String,
    @JsonFormat(pattern = "E, dd MMM yyyy HH:mm:ss Z")
    @JacksonXmlProperty(isAttribute = true, localName = "date")
    val joinDate: LocalDate,
)
