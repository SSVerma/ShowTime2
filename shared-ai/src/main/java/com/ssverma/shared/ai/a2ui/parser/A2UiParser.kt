package com.ssverma.shared.ai.a2ui.parser

import com.ssverma.shared.ai.a2ui.model.A2UiAction
import com.ssverma.shared.ai.a2ui.model.A2UiChip
import com.ssverma.shared.ai.a2ui.model.A2UiComponent
import com.ssverma.shared.ai.a2ui.model.A2UiDocument
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

@Singleton
class A2UiParser @Inject constructor() {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
    }

    /**
     * Parses raw LLM text into an [A2UiDocument].
     *
     * Separates conversational Markdown text from enclosed ```a2ui / ```json UI code blocks.
     * Guaranteed never to leak raw JSON code to the user.
     */
    fun parse(rawText: String): A2UiDocument {
        val trimmed = rawText.trim()
        if (trimmed.isEmpty()) {
            return A2UiDocument(
                id = UUID.randomUUID().toString(),
                components = emptyList(),
            )
        }

        val fenceTag = findFenceTag(trimmed)
        if (fenceTag != null) {
            val fenceStartIndex = trimmed.indexOf(fenceTag)
            val preFenceText = trimmed.substring(0, fenceStartIndex).trim()
            val afterTagIndex = fenceStartIndex + fenceTag.length

            val contentStartIndex =
                trimmed.indexOf('\n', startIndex = afterTagIndex).let { newline ->
                    if (newline != -1) newline + 1 else afterTagIndex
                }

            val closingFenceIndex = trimmed.indexOf("```", startIndex = contentStartIndex)
            val rawJsonPayload: String
            val postFenceText: String

            if (closingFenceIndex != -1) {
                rawJsonPayload = trimmed.substring(contentStartIndex, closingFenceIndex).trim()
                postFenceText = trimmed.substring(closingFenceIndex + 3).trim()
            } else {
                // Truncated or streaming code fence
                rawJsonPayload = trimmed.substring(contentStartIndex).trim()
                postFenceText = ""
            }

            val components = mutableListOf<A2UiComponent>()

            if (preFenceText.isNotEmpty()) {
                components.add(
                    A2UiComponent.Text(
                        id = UUID.randomUUID().toString(),
                        content = deduplicateRepetitions(preFenceText),
                    )
                )
            }

            var docId: String? = null
            var docTitle: String? = null

            if (rawJsonPayload.isNotEmpty()) {
                val (id, title, parsedComponents) = parseJsonPayload(rawJsonPayload)
                docId = id
                docTitle = title
                components.addAll(parsedComponents)
            }

            val cleanPostFenceText = when {
                postFenceText.contains("```") -> postFenceText.substring(
                    0,
                    postFenceText.indexOf("```")
                ).trim()

                postFenceText.contains("{") -> postFenceText.substring(
                    0,
                    postFenceText.indexOf('{')
                ).trim()

                else -> postFenceText.trim()
            }

            if (cleanPostFenceText.isNotEmpty() && !cleanPostFenceText.equals(
                    preFenceText,
                    ignoreCase = true
                )
            ) {
                components.add(
                    A2UiComponent.Text(
                        id = UUID.randomUUID().toString(),
                        content = deduplicateRepetitions(cleanPostFenceText),
                    )
                )
            }

            return A2UiDocument(
                id = docId ?: UUID.randomUUID().toString(),
                title = docTitle,
                components = components,
            )
        }

        // Check if rawText contains an unfenced JSON object (e.g. { "type": ... })
        val jsonStartIndex = findJsonStartIndex(trimmed)
        if (jsonStartIndex != -1) {
            val preJsonText = trimmed.substring(0, jsonStartIndex).trim()
            val rawJson = trimmed.substring(jsonStartIndex).trim()
            val parsed = parseJsonPayload(rawJson)

            if (parsed.components.isNotEmpty()) {
                val components = mutableListOf<A2UiComponent>()
                if (preJsonText.isNotEmpty()) {
                    components.add(
                        A2UiComponent.Text(
                            id = UUID.randomUUID().toString(),
                            content = deduplicateRepetitions(preJsonText),
                        )
                    )
                }
                components.addAll(parsed.components)
                return A2UiDocument(
                    id = parsed.id ?: UUID.randomUUID().toString(),
                    title = parsed.title,
                    components = components,
                )
            }
        }

        // Pure conversational text response
        return A2UiDocument(
            id = UUID.randomUUID().toString(),
            components = listOf(
                A2UiComponent.Text(
                    id = UUID.randomUUID().toString(),
                    content = deduplicateRepetitions(trimmed),
                )
            ),
        )
    }

    private fun deduplicateRepetitions(text: String): String {
        val trimmed = text.trim()
        if (trimmed.length < 20) return trimmed

        // Case 1: Exact duplicate halves "XYZ.XYZ." or "XYZ. XYZ."
        val half = trimmed.length / 2
        val firstHalf = trimmed.substring(0, half).trim()
        val secondHalf = trimmed.substring(half).trim()
        if (firstHalf.equals(secondHalf, ignoreCase = true)) {
            return firstHalf
        }

        // Case 2: Adjacent duplicate sentences
        val sentences = trimmed.split(Regex("(?<=[.!?])\\s+"))
        if (sentences.size >= 2) {
            val deduped = mutableListOf<String>()
            for (sentence in sentences) {
                if (deduped.isEmpty() || !sentence.equals(deduped.last(), ignoreCase = true)) {
                    deduped.add(sentence)
                }
            }
            if (deduped.size < sentences.size) {
                return deduped.joinToString(" ")
            }
        }

        return trimmed
    }

    private fun findFenceTag(text: String): String? {
        val candidates = listOf("```a2ui", "```json", "```A2UI", "```JSON", "```")
        return candidates.firstOrNull { text.contains(it) }
    }

    private fun findJsonStartIndex(text: String): Int {
        val firstBrace = text.indexOf('{')
        if (firstBrace == -1) return -1
        val candidate = text.substring(firstBrace).lowercase()
        return if (candidate.contains("\"type\"") || candidate.contains("\"media_carousel\"") || candidate.contains(
                "\"items\""
            )
        ) {
            firstBrace
        } else {
            -1
        }
    }

    private data class ParsedJson(
        val id: String? = null,
        val title: String? = null,
        val components: List<A2UiComponent> = emptyList(),
    )

    private fun parseJsonPayload(rawJson: String): ParsedJson {
        val sanitized = sanitizeJsonPayload(rawJson)
        if (sanitized.isBlank()) return ParsedJson()

        return try {
            val rootElement = json.parseToJsonElement(sanitized)
            if (rootElement is JsonObject) {
                val normalized = rootElement.normalize()
                val docId = (normalized["id"] as? JsonPrimitive)?.content
                val docTitle = (normalized["title"] as? JsonPrimitive)?.content
                val components = extractComponentsFromElement(rootElement)
                ParsedJson(
                    id = docId,
                    title = docTitle,
                    components = components,
                )
            } else {
                ParsedJson()
            }
        } catch (_: Exception) {
            ParsedJson()
        }
    }

    /**
     * Sanitizes incomplete or truncated JSON payloads by removing dangling incomplete tokens
     * and balancing unclosed braces and brackets in exact LIFO nesting order.
     */
    private fun sanitizeJsonPayload(rawJson: String): String {
        var trimmed = rawJson.trim()
        val firstBrace = trimmed.indexOf('{')
        if ((firstBrace != -1) && !trimmed.startsWith("{")) {
            trimmed = trimmed.substring(firstBrace)
        }
        if (!trimmed.startsWith("{") && !trimmed.startsWith("[")) {
            return ""
        }

        // Trim dangling incomplete property names or values at the tail
        trimmed = trimDanglingTokens(trimmed)

        val openStack = ArrayDeque<Char>()
        var inString = false
        var isEscape = false

        for (char in trimmed) {
            if (isEscape) {
                isEscape = false
                continue
            }
            if (char == '\\') {
                isEscape = true
                continue
            }
            if (char == '"') {
                inString = !inString
                continue
            }
            if (!inString) {
                when (char) {
                    '{' -> openStack.addLast('{')
                    '[' -> openStack.addLast('[')
                    '}' -> if (openStack.isNotEmpty() && openStack.last() == '{') openStack.removeLast()
                    ']' -> if (openStack.isNotEmpty() && openStack.last() == '[') openStack.removeLast()
                }
            }
        }

        val builder = java.lang.StringBuilder(trimmed)
        if (inString) {
            builder.append('"')
        }

        // Close unclosed tokens in reverse (LIFO) nesting order
        while (openStack.isNotEmpty()) {
            when (openStack.removeLast()) {
                '{' -> builder.append('}')
                '[' -> builder.append(']')
            }
        }

        return builder.toString()
    }

    private fun trimDanglingTokens(text: String): String {
        var result = text.trimEnd()
        // If it ends with an unclosed quote or property, strip back past the last comma or opening bracket/brace
        val lastQuote = result.lastIndexOf('"')
        if (lastQuote != -1) {
            val quotesCount = result.count { it == '"' }
            if (quotesCount % 2 != 0) {
                // Odd number of quotes = unclosed string token
                result = result.substring(0, lastQuote).trimEnd()
            }
        }

        // Strip trailing comma or colon if left exposed
        while (result.endsWith(",") || result.endsWith(":") || result.endsWith(" ")) {
            result = result.dropLast(1).trimEnd()
        }
        return result
    }

    private fun JsonObject.normalize(): Map<String, JsonElement> {
        return entries.associateBy(
            keySelector = { (k, _) -> k.trim().lowercase().replace("_", "") },
            valueTransform = { (_, v) -> v },
        )
    }

    private fun extractComponentsFromElement(element: JsonElement): List<A2UiComponent> {
        if (element !is JsonObject) return emptyList()
        val normalized = element.normalize()

        // Case A: Full A2UiDocument with components array
        val componentsArray = (normalized["components"] ?: element["components"]) as? JsonArray
        if (componentsArray != null) {
            return componentsArray.mapNotNull { parseSingleComponent(it) }
        }

        // Case B: Direct single component (like Carousel, MediaCard, ChipGroup)
        val singleComponent = parseSingleComponent(element)
        if (singleComponent != null) {
            return listOf(singleComponent)
        }

        // Case C: Top-level object containing "mediacarousel", "carousel", or "items"
        val itemsArray = (normalized["mediacarousel"] ?: normalized["items"] ?: normalized["movies"]
        ?: normalized["results"]) as? JsonArray
        if (itemsArray != null) {
            val title = (normalized["title"] as? JsonPrimitive)?.content.orEmpty()
            val mediaCards = itemsArray.mapNotNull { parseMediaCard(it) }
            if (mediaCards.isNotEmpty()) {
                return listOf(
                    A2UiComponent.MediaCarousel(
                        id = (normalized["id"] as? JsonPrimitive)?.content ?: UUID.randomUUID()
                            .toString(),
                        title = title,
                        items = mediaCards,
                    )
                )
            }
        }

        return emptyList()
    }

    private fun parseSingleComponent(element: JsonElement): A2UiComponent? {
        if (element !is JsonObject) return null
        val normalized = element.normalize()
        val type = (normalized["type"] as? JsonPrimitive)?.content?.lowercase()?.replace("_", "")

        return when (type) {
            "mediacarousel", "carousel" -> {
                val itemsArray =
                    (normalized["items"] ?: normalized["mediacarousel"] ?: normalized["movies"]
                    ?: normalized["results"]) as? JsonArray
                val mediaCards = itemsArray?.mapNotNull { parseMediaCard(it) } ?: emptyList()
                val title = (normalized["title"] as? JsonPrimitive)?.content.orEmpty()
                A2UiComponent.MediaCarousel(
                    id = (normalized["id"] as? JsonPrimitive)?.content ?: UUID.randomUUID()
                        .toString(),
                    title = title,
                    items = mediaCards,
                )
            }

            "mediacard", "movie", "card" -> parseMediaCard(element)
            "chipgroup", "chips" -> {
                val chipsArray = (normalized["chips"] ?: normalized["items"]) as? JsonArray
                val chips = chipsArray?.mapNotNull { parseChip(it) } ?: emptyList()
                val title = (normalized["title"] as? JsonPrimitive)?.content
                A2UiComponent.ChipGroup(
                    id = (normalized["id"] as? JsonPrimitive)?.content ?: UUID.randomUUID()
                        .toString(),
                    title = title,
                    chips = chips,
                )
            }

            "text" -> {
                val content = (normalized["content"] as? JsonPrimitive)?.content
                    ?: (normalized["text"] as? JsonPrimitive)?.content
                    ?: return null
                A2UiComponent.Text(
                    id = (normalized["id"] as? JsonPrimitive)?.content ?: UUID.randomUUID()
                        .toString(),
                    content = content,
                    isEmphasized = (normalized["isemphasized"] as? JsonPrimitive)?.content?.toBoolean()
                        ?: false,
                )
            }

            else -> {
                val itemsArray = (normalized["items"] ?: normalized["mediacarousel"]
                ?: normalized["movies"]) as? JsonArray
                if (itemsArray != null) {
                    val mediaCards = itemsArray.mapNotNull { parseMediaCard(it) }
                    A2UiComponent.MediaCarousel(
                        id = (normalized["id"] as? JsonPrimitive)?.content ?: UUID.randomUUID()
                            .toString(),
                        title = (normalized["title"] as? JsonPrimitive)?.content.orEmpty(),
                        items = mediaCards,
                    )
                } else null
            }
        }
    }

    private fun parseMediaCard(element: JsonElement): A2UiComponent.MediaCard? {
        if (element !is JsonObject) return null
        val normalized = element.normalize()

        val title = (normalized["title"] as? JsonPrimitive)?.content
            ?: (normalized["name"] as? JsonPrimitive)?.content
            ?: (normalized["movietitle"] as? JsonPrimitive)?.content
            ?: "Featured Film"

        val id = (normalized["id"] as? JsonPrimitive)?.content ?: UUID.randomUUID().toString()
        val tmdbId = parseTmdbId(normalized["tmdbid"] ?: normalized["movieid"] ?: normalized["id"])
        val posterPath = (normalized["posterpath"] as? JsonPrimitive)?.content
            ?: (normalized["poster"] as? JsonPrimitive)?.content
            ?: (normalized["image"] as? JsonPrimitive)?.content
        val releaseYear = (normalized["releaseyear"] as? JsonPrimitive)?.content
            ?: (normalized["year"] as? JsonPrimitive)?.content
        val rating = (normalized["rating"] as? JsonPrimitive)?.floatOrNull ?: 0.0f
        val overview = (normalized["overview"] as? JsonPrimitive)?.content
            ?: (normalized["description"] as? JsonPrimitive)?.content

        return A2UiComponent.MediaCard(
            id = id,
            tmdbId = tmdbId,
            title = title,
            posterPath = posterPath,
            releaseYear = releaseYear,
            rating = rating,
            overview = overview,
        )
    }

    private fun parseChip(element: JsonElement): A2UiChip? {
        if (element !is JsonObject) return null
        val normalized = element.normalize()
        val label = (normalized["label"] as? JsonPrimitive)?.content
            ?: (normalized["name"] as? JsonPrimitive)?.content
            ?: return null
        val id = (normalized["id"] as? JsonPrimitive)?.content ?: UUID.randomUUID().toString()
        val prompt = (normalized["prompt"] as? JsonPrimitive)?.content
        val action = if (prompt != null) A2UiAction.PromptQuery(prompt) else null

        return A2UiChip(
            id = id,
            label = label,
            action = action,
        )
    }

    private fun parseTmdbId(element: JsonElement?): Int {
        if (element == null) return (1000..9999).random()
        return when (element) {
            is JsonPrimitive -> {
                element.intOrNull
                    ?: element.content.filter { it.isDigit() }.toIntOrNull()
                    ?: abs(element.content.hashCode() % 1_000_000)
            }

            else -> (1000..9999).random()
        }
    }
}

