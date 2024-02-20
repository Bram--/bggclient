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
import com.fasterxml.jackson.databind.annotation.JsonDeserialize
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty
import java.time.LocalDateTime
import org.audux.bgg.common.SubType

/** 'Hack' as many BGG API values are stored in an attribute e.g. '<element value='2.123 />'' */
data class WrappedLocalDateTime(
    @JacksonXmlProperty(isAttribute = true)
    @JsonFormat(pattern = "E, dd MMM yyyy HH:mm:ss Z")
    val value: LocalDateTime,
)

/** 'Hack' as many BGG API values are stored in an attribute e.g. '<element value='2.123 />'' */
data class WrappedSubType(
    @JsonDeserialize(using = SubTypeDeserializer::class) val value: SubType,
)
