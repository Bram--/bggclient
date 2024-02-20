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

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import org.audux.bgg.common.FamilyType
import org.audux.bgg.common.ForumListType
import org.audux.bgg.common.PlayThingType
import org.audux.bgg.common.SubType
import org.audux.bgg.common.ThingType

/** Deserializes thing types to the associated [ThingType] enum value. */
internal class ThingTypeDeserializer : JsonDeserializer<ThingType>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?) =
        ThingType.fromParam(parser?.valueAsString)
}

/** Deserializes '0' sa False and '1' as True. */
internal class NumberToBooleanDeserializer : JsonDeserializer<Boolean>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?) =
        parser?.valueAsString == "1"
}

/** Deserializes and trims strings. */
internal class TrimmedStringDeserializer : JsonDeserializer<String?>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?) =
        parser?.valueAsString?.trim()
}

/** Deserializes family types to the associated [FamilyType] enum value. */
internal class FamilyTypeDeserializer : JsonDeserializer<FamilyType>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?) =
        FamilyType.fromParam(parser?.valueAsString)
}

/** Deserializes type in ForumList response to [ForumListType]. */
internal class ForumListTypeDeserializer : JsonDeserializer<ForumListType>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?) =
        ForumListType.fromParam(parser?.valueAsString)
}

/** Deserializes type in Plays response to [PlayThingType]. */
internal class PlayThingTypeDeserializer : JsonDeserializer<PlayThingType>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?) =
        PlayThingType.fromParam(parser?.valueAsString)
}

/** Deserializes type in Plays response to [org.audux.bgg.common.SubType]. */
internal class SubTypeDeserializer : JsonDeserializer<SubType>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?) =
        SubType.fromParam(parser?.valueAsString)
}

/** Deserializes `<elementName value="Value" />` objects into a `String("Value")` property. */
internal class WrappedStringDeserializer : JsonDeserializer<String?>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?) =
        readWrappedValue(parser) { it.valueAsString }
}

/** Deserializes `<elementName value="123" />` objects into an `Int(123)` property. */
internal class WrappedIntDeserializer : JsonDeserializer<Int?>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?) =
        readWrappedValue(parser) { it.valueAsInt }
}

/** Deserializes `<elementName value="123.123" />` objects into an `Double(123.123)` property. */
internal class WrappedDoubleDeserializer : JsonDeserializer<Double?>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?) =
        readWrappedValue(parser) { it.valueAsDouble }
}

/**
 * Deserializes `<elementName value="2012-10-12" />` objects into a `LocalDate.of(2012, 10, 12)
 * property.
 */
internal class WrappedLocalDateDeserializer : JsonDeserializer<LocalDate?>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?) =
        readWrappedValue(parser) {
            it.valueAsString
                ?.takeIf { str -> str.isNotBlank() }
                ?.let { str -> LocalDate.parse(str) }
        }
}

/**
 * Deserializes `<elementName value="2012-10-12" />` objects into a `LocalDate.of(2012, 10, 12)
 * property.
 */
internal class WrappedLocalDateTimeDeserializer : JsonDeserializer<LocalDate?>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?) =
        readWrappedValue(parser) {
            val formatter = DateTimeFormatter.ofPattern("E, dd MMM yyyy HH:mm:ss Z")
            LocalDate.parse(it.valueAsString, formatter)
        }
}

/** Deserializes and trims strings. */
private fun <T> readWrappedValue(parser: JsonParser?, read: (JsonParser) -> T): T? =
    parser?.let {
        var value: T? = null
        while (it.nextToken() != JsonToken.END_OBJECT) {
            if (it.currentToken == JsonToken.VALUE_STRING) {
                value = read(it)
            }
        }

        return value
    }
