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
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import org.audux.bgg.common.FamilyType
import org.audux.bgg.common.ForumListType
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

/** Deserializes type in ForumList response to ForumListType. */
internal class ForumListTypeDeserializer : JsonDeserializer<ForumListType>() {
    override fun deserialize(parser: JsonParser?, context: DeserializationContext?) =
        ForumListType.fromParam(parser?.valueAsString)
}
