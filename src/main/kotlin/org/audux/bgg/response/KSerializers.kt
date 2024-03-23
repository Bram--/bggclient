package org.audux.bgg.response

import java.net.URI
import java.time.LocalDate
import java.time.LocalDateTime
import kotlinx.serialization.InternalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SealedClassSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/** [KSerializer] for a [LocalDate]. */
class LocalDateSerializer : KSerializer<LocalDate> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDate", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDate) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDate = LocalDate.parse(decoder.decodeString())
}

/** [KSerializer] for a [LocalDateTime]. */
class LocalDateTimeSerializer : KSerializer<LocalDateTime> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): LocalDateTime =
        LocalDateTime.parse(decoder.decodeString())
}

/** [KSerializer] for a [URI]. */
class URISerializer : KSerializer<URI> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("URI", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: URI) {
        encoder.encodeString(value.toString())
    }

    override fun deserialize(decoder: Decoder): URI = URI.create(decoder.decodeString())
}

@OptIn(InternalSerializationApi::class)
class PollSerializer : KSerializer<Poll> {
    private val serializer =
        SealedClassSerializer(
            "Poll",
            Poll::class,
            arrayOf(
                LanguageDependencePoll::class,
                PlayerAgePoll::class,
                NumberOfPlayersPoll::class
            ),
            arrayOf(
                LanguageDependencePoll.serializer(),
                PlayerAgePoll.serializer(),
                NumberOfPlayersPoll.serializer(),
            ),
        )

    override val descriptor: SerialDescriptor = serializer.descriptor

    override fun deserialize(decoder: Decoder): Poll {
        return serializer.deserialize(decoder)
    }

    override fun serialize(encoder: Encoder, value: Poll) {
        serializer.serialize(encoder, value)
    }
}
