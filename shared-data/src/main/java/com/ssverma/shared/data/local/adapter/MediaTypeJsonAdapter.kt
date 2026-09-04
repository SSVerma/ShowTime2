package com.ssverma.shared.data.local.adapter

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializationContext
import com.google.gson.JsonSerializer
import com.ssverma.shared.domain.model.MediaType
import java.lang.reflect.Type

class MediaTypeJsonAdapter : JsonSerializer<MediaType>, JsonDeserializer<MediaType> {
    override fun serialize(
        src: MediaType,
        typeOfSrc: Type,
        context: JsonSerializationContext
    ): JsonElement {
        val value = when (src) {
            MediaType.Movie -> "movie"
            MediaType.Tv -> "tv"
            MediaType.Person -> "person"
            else -> "unknown"
        }
        return JsonPrimitive(value)
    }

    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): MediaType {
        return when (json.asString.lowercase()) {
            "movie" -> MediaType.Movie
            "tv" -> MediaType.Tv
            "person" -> MediaType.Person
            else -> MediaType.Unknown
        }
    }
}
