package com.ssverma.shared.ai.service

import com.ssverma.core.ai.engine.OnDeviceAgentEngine
import com.ssverma.core.ai.model.AiAgentEvent
import com.ssverma.core.ai.model.AiModelAvailability
import com.ssverma.core.ai.session.OnDeviceAgentSession
import com.ssverma.shared.ai.a2ui.model.A2UiComponent
import com.ssverma.shared.ai.a2ui.model.A2UiDocument
import com.ssverma.shared.ai.a2ui.parser.A2UiParser
import com.ssverma.shared.ai.agent.CinephileAgentConfig
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.model.discovery.DiscoveryDecade
import com.ssverma.shared.domain.model.discovery.DiscoveryLanguage
import com.ssverma.shared.domain.model.discovery.DiscoverySortOrder
import com.ssverma.shared.domain.model.discovery.DiscoveryStudioHub
import com.ssverma.shared.domain.model.discovery.DiscoveryVibePreset
import com.ssverma.shared.domain.model.discovery.UniversalDiscoveryFilter
import com.ssverma.shared.domain.model.discovery.UniversalMediaItem
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.DiscoveryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.abs

sealed interface CinephileResponseState {
    data class Streaming(val partialText: String, val activeTool: String? = null) :
        CinephileResponseState

    data class Success(val document: A2UiDocument) : CinephileResponseState
    data class Failure(val message: String, val isRecoverable: Boolean) : CinephileResponseState
}

@Singleton
class CinephileAssistantService @Inject constructor(
    private val agentEngine: OnDeviceAgentEngine,
    private val a2UiParser: A2UiParser,
    private val discoveryRepository: DiscoveryRepository? = null,
    private val appConfigRepository: AppConfigRepository? = null,
) : AutoCloseable {

    private var activeSession: OnDeviceAgentSession? = null

    suspend fun checkAvailability(): AiModelAvailability {
        return agentEngine.checkAvailability()
    }

    fun getOrCreateSession(sessionId: String = CinephileAgentConfig.SESSION_ID_DEFAULT): OnDeviceAgentSession {
        activeSession?.let { currentSession ->
            if (currentSession.sessionId == sessionId) {
                return currentSession
            }
            currentSession.close()
        }

        val newSession = agentEngine.createSession(
            sessionId = sessionId,
            agentName = CinephileAgentConfig.AGENT_NAME,
            systemInstructions = CinephileAgentConfig.SYSTEM_INSTRUCTIONS,
            enabledAppFunctionIds = CinephileAgentConfig.ENABLED_TOOL_IDS
        )
        activeSession = newSession
        return newSession
    }


    private fun isFactualOrTriviaQuery(prompt: String): Boolean {
        val lower = prompt.lowercase()
        return lower.startsWith("who ") ||
                lower.contains("who created") ||
                lower.contains("who directed") ||
                lower.contains("who wrote") ||
                lower.contains("who starred") ||
                lower.contains("who played") ||
                lower.contains("who is") ||
                lower.contains("what is this") ||
                lower.contains("explain") ||
                lower.contains("tell me about the plot") ||
                lower.contains("what is the plot")
    }

    private fun hasDiscoveryIntent(prompt: String): Boolean {
        if (isFactualOrTriviaQuery(prompt)) {
            return false
        }
        val lower = prompt.lowercase()
        val intentKeywords = listOf(
            "movie",
            "movies",
            "film",
            "films",
            "cinema",
            "show",
            "shows",
            "series",
            "watch",
            "recommend",
            "recommendation",
            "recommendations",
            "suggest",
            "suggestions",
            "latest",
            "recent",
            "new",
            "released",
            "launch",
            "launched",
            "upcoming",
            "premier",
            "top rated",
            "best",
            "acclaimed",
            "hit",
            "blockbuster",
            "classic",
            "all",
            "action",
            "comedy",
            "horror",
            "romance",
            "romantic",
            "animation",
            "animated",
            "anime",
            "doremon",
            "doraemon",
            "sci-fi",
            "scifi",
            "thriller",
            "crime",
            "drama",
            "fantasy",
            "adventure",
            "mystery",
            "family",
            "marvel",
            "mcu",
            "dc",
            "batman",
            "superman",
            "avenger",
            "avengers",
            "pixar",
            "a24",
            "ghibli",
            "star wars",
            "spider",
            "spiderman",
            "iron man",
            "deadpool",
            "wolverine",
            "oppenheimer",
            "interstellar",
            "dune",
            "matrix",
            "godfather",
            "avatar",
            "bollywood",
            "hindi",
            "korean",
            "japanese",
            "french",
            "spanish"
        )
        return intentKeywords.any { lower.contains(it) }
    }

    private suspend fun resolveFilterFromPrompt(prompt: String): UniversalDiscoveryFilter {
        val lower = prompt.lowercase()

        val studioHub = when {
            lower.contains("marvel") || lower.contains("mcu") || lower.contains("avenger") || lower.contains(
                "iron man"
            ) || lower.contains("spider") -> DiscoveryStudioHub.MARVEL

            lower.contains("dc") || lower.contains("batman") || lower.contains("superman") || lower.contains(
                "gotham"
            ) || lower.contains("warner") -> DiscoveryStudioHub.WARNER_BROS

            lower.contains("a24") -> DiscoveryStudioHub.A24
            lower.contains("pixar") -> DiscoveryStudioHub.PIXAR
            lower.contains("ghibli") || lower.contains("miyazaki") -> DiscoveryStudioHub.STUDIO_GHIBLI
            lower.contains("neon") -> DiscoveryStudioHub.NEON
            lower.contains("universal") -> DiscoveryStudioHub.UNIVERSAL
            lower.contains("paramount") -> DiscoveryStudioHub.PARAMOUNT
            lower.contains("blumhouse") -> DiscoveryStudioHub.BLUMHOUSE
            else -> null
        }

        val vibePreset = when {
            lower.contains("mind bending") || lower.contains("mind-bending") || lower.contains("psychological") -> DiscoveryVibePreset.MIND_BENDING
            lower.contains("dark") || lower.contains("gritty") || lower.contains("noir") -> DiscoveryVibePreset.DARK_AND_GRITTY
            lower.contains("fun") || lower.contains("comedy") || lower.contains("funny") -> DiscoveryVibePreset.PURE_FUN
            lower.contains("sci-fi") || lower.contains("scifi") || lower.contains("space") || lower.contains(
                "epic"
            ) -> DiscoveryVibePreset.EPIC_WORLDS

            lower.contains("horror") || lower.contains("scary") || lower.contains("chills") -> DiscoveryVibePreset.LATE_NIGHT_CHILLS
            lower.contains("masterpiece") || lower.contains("top rated") || lower.contains("best") -> DiscoveryVibePreset.MASTERPIECES
            else -> DiscoveryVibePreset.ALL
        }

        val sortOrder = when {
            lower.contains("latest") || lower.contains("recent") || lower.contains("new") || lower.contains(
                "launch"
            ) || lower.contains("released") || lower.contains("upcoming") -> DiscoverySortOrder.RELEASE_DATE_DESC

            lower.contains("top rated") || lower.contains("best") || lower.contains("highest rated") || lower.contains(
                "acclaimed"
            ) -> DiscoverySortOrder.VOTE_AVERAGE_DESC

            lower.contains("most reviewed") || lower.contains("most voted") -> DiscoverySortOrder.VOTE_COUNT_DESC
            else -> DiscoverySortOrder.POPULARITY_DESC
        }

        val decade = when {
            lower.contains("2024") || lower.contains("2025") || lower.contains("2026") || lower.contains(
                "2020s"
            ) ||
                    lower.contains("last year") || lower.contains("last yer") || lower.contains("this year") -> DiscoveryDecade.TWENTIES_2020S

            lower.contains("2010s") || lower.contains("2010") -> DiscoveryDecade.TENS_2010S
            lower.contains("2000s") || lower.contains("2000") -> DiscoveryDecade.AUGHTS_2000S
            lower.contains("90s") || lower.contains("1990") -> DiscoveryDecade.NINETIES_1990S
            lower.contains("80s") || lower.contains("1980") -> DiscoveryDecade.EIGHTIES_1980S
            lower.contains("70s") || lower.contains("1970") -> DiscoveryDecade.SEVENTIES_1970S
            lower.contains("classic") || lower.contains("golden age") -> DiscoveryDecade.GOLDEN_AGE
            else -> DiscoveryDecade.ALL_TIME
        }

        fun hasKeyword(keyword: String): Boolean {
            val idx = lower.indexOf(keyword)
            if (idx == -1) return false
            val prefix = lower.substring(maxOf(0, idx - 10), idx)
            val isNegated =
                prefix.contains("no ") || prefix.contains("not ") || prefix.contains("without ") || prefix.contains(
                    "non-"
                )
            return !isNegated
        }

        val explicitGenreIds = mutableSetOf<Int>()
        if (hasKeyword("action") || hasKeyword("fight") || hasKeyword("superhero")) explicitGenreIds.add(
            28
        )
        if (hasKeyword("comedy") || hasKeyword("funny") || hasKeyword("humor") || hasKeyword("hilarious")) explicitGenreIds.add(
            35
        )
        if (hasKeyword("horror") || hasKeyword("scary") || hasKeyword("ghost") || hasKeyword("spooky")) explicitGenreIds.add(
            27
        )
        if (hasKeyword("romance") || hasKeyword("romantic") || hasKeyword("love")) explicitGenreIds.add(
            10749
        )
        if (hasKeyword("animation") || hasKeyword("animated") || hasKeyword("anime") || hasKeyword("cartoon") || hasKeyword(
                "doremon"
            ) || hasKeyword("doraemon")
        ) {
            explicitGenreIds.add(16)
        }
        if (hasKeyword("family") || hasKeyword("kids") || hasKeyword("children") || hasKeyword("doremon") || hasKeyword(
                "doraemon"
            )
        ) {
            explicitGenreIds.add(10751)
        }
        if (hasKeyword("documentary") || hasKeyword("docu")) explicitGenreIds.add(99)
        if (hasKeyword("sci-fi") || hasKeyword("scifi") || hasKeyword("science fiction") || hasKeyword(
                "space"
            )
        ) explicitGenreIds.add(878)
        if (hasKeyword("thriller") || hasKeyword("suspense")) explicitGenreIds.add(53)
        if (hasKeyword("crime") || hasKeyword("mafia") || hasKeyword("gangster") || hasKeyword("noir")) explicitGenreIds.add(
            80
        )
        if (hasKeyword("drama")) explicitGenreIds.add(18)
        if (hasKeyword("fantasy") || hasKeyword("magic")) explicitGenreIds.add(14)
        if (hasKeyword("adventure") || hasKeyword("journey")) explicitGenreIds.add(12)
        if (hasKeyword("mystery") || hasKeyword("whodunit")) explicitGenreIds.add(9648)
        if (hasKeyword("musical") || hasKeyword("music film")) explicitGenreIds.add(10402)
        if (hasKeyword("war ") || hasKeyword("war movie") || hasKeyword("military")) explicitGenreIds.add(
            10752
        )
        if (hasKeyword("western") || hasKeyword("cowboy")) explicitGenreIds.add(37)

        val originCountry = when {
            lower.contains("india") || lower.contains("indian") || lower.contains("bollywood") -> "IN"
            lower.contains("korea") || lower.contains("korean") || lower.contains("k-drama") -> "KR"
            lower.contains("japan") || lower.contains("japanese") || lower.contains("anime") -> "JP"
            lower.contains("france") || lower.contains("french") -> "FR"
            lower.contains("spain") || lower.contains("spanish") -> "ES"
            lower.contains("uk") || lower.contains("britain") || lower.contains("british") -> "GB"
            else -> null
        }

        val language = when {
            lower.contains("hindi") || lower.contains("bollywood") -> DiscoveryLanguage.HINDI
            lower.contains("korea") || lower.contains("korean") || lower.contains("k-drama") -> DiscoveryLanguage.KOREAN
            lower.contains("japan") || lower.contains("japanese") || lower.contains("anime") || lower.contains(
                "doremon"
            ) || lower.contains("doraemon") -> DiscoveryLanguage.JAPANESE

            lower.contains("french") || lower.contains("france") -> DiscoveryLanguage.FRENCH
            lower.contains("spanish") || lower.contains("spain") -> DiscoveryLanguage.SPANISH
            else -> DiscoveryLanguage.ALL
        }

        // Preferences override rule: explicit genres in prompt strictly override default seeded preferences
        val finalGenreIds = if (explicitGenreIds.isNotEmpty()) {
            explicitGenreIds
        } else if (studioHub != null || originCountry != null || language != DiscoveryLanguage.ALL) {
            // When studio, franchise, region, or foreign language is specified, do not over-constrain with onboarding genres
            emptySet()
        } else {
            val seeded = appConfigRepository?.userSeededGenres?.firstOrNull().orEmpty()
            if (seeded.size > 2) {
                // Pick 1-2 seeded genres with hash rotation so TMDB discover does not over-constrain with AND
                val seedList = seeded.toList()
                val index = abs(prompt.hashCode()) % seedList.size
                setOf(seedList[index])
            } else {
                seeded
            }
        }

        val explicitProviderIds = mutableSetOf<Int>()
        if (lower.contains("netflix")) explicitProviderIds.add(8)
        if (lower.contains("prime") || lower.contains("amazon")) explicitProviderIds.add(9)
        if (lower.contains("disney")) explicitProviderIds.add(337)
        if (lower.contains("apple tv") || lower.contains("apple plus") || lower.contains("appletv")) explicitProviderIds.add(
            350
        )
        if (lower.contains("hulu")) explicitProviderIds.add(15)
        if (lower.contains("max") || lower.contains("hbo")) explicitProviderIds.add(1899)
        if (lower.contains("paramount")) explicitProviderIds.add(531)
        if (lower.contains("peacock")) explicitProviderIds.add(386)

        val watchRegion = when {
            lower.contains("india") || lower.contains("indian") || lower.contains("bollywood") || lower.contains(
                "hindi"
            ) -> "IN"

            lower.contains("uk") || lower.contains("britain") || lower.contains("british") -> "GB"
            lower.contains("canada") -> "CA"
            lower.contains("australia") -> "AU"
            else -> appConfigRepository?.watchProviderRegion?.value?.ifBlank { "US" } ?: "US"
        }

        return UniversalDiscoveryFilter(
            studioHub = studioHub,
            vibePreset = vibePreset,
            sortOrder = sortOrder,
            decade = decade,
            selectedGenreIds = finalGenreIds,
            selectedProviderIds = explicitProviderIds,
            watchRegion = watchRegion,
            originCountry = originCountry,
            language = language,
        )
    }

    private fun isFollowUpRefinement(prompt: String): Boolean {
        val lower = prompt.lowercase().trim()
        val refinementStarters = listOf(
            "in ", "from ", "about ", "what about", "how about",
            "any ", "more ", "instead", "too", "also", "for ",
            "with ", "im asking", "i'm asking", "asking", "only "
        )
        val isStartedWithRefinement = refinementStarters.any { lower.startsWith(it) }
        val isShortRefinement = lower.length < 30 && (
                lower.contains("in india") || lower.contains("in hindi") ||
                        lower.contains("in uk") || lower.contains("in us") ||
                        lower.contains("bollywood") || lower.contains("hollywood") ||
                        lower.contains("on netflix") || lower.contains("on prime")
                )
        return isStartedWithRefinement || isShortRefinement
    }

    private fun findLastDiscoveryPrompt(priorPrompts: List<String>): String? {
        return priorPrompts.reversed().firstOrNull { prompt ->
            hasDiscoveryIntent(prompt) && !isFollowUpRefinement(prompt)
        }
    }

    private fun extractSearchKeyword(prompt: String): String {
        val lower = prompt.lowercase()
        if (lower.contains("india") || lower.contains("bollywood")) return "India"
        if (lower.contains("korea") || lower.contains("k-drama")) return "Korean"
        if (lower.contains("anime") || lower.contains("doremon") || lower.contains("doraemon")) return "Anime"
        if (lower.contains("marvel") || lower.contains("mcu") || lower.contains("avenger")) return "Marvel"
        if (lower.contains("dc") || lower.contains("batman") || lower.contains("superman")) return "Batman"
        if (lower.contains("star wars")) return "Star Wars"
        return lower
            .replace("give me", "")
            .replace("show me", "")
            .replace("recommend", "")
            .replace("suggest", "")
            .replace("what are", "")
            .replace("what is", "")
            .replace("tell me about", "")
            .replace("all", "")
            .replace("new", "")
            .replace("latest", "")
            .replace("recent", "")
            .replace("best", "")
            .replace("in last yer", "")
            .replace("in last year", "")
            .replace("movies", "")
            .replace("movie", "")
            .replace("films", "")
            .replace("film", "")
            .trim()
    }

    private suspend fun fetchCatalogGroundingContext(
        prompt: String,
        priorUserPrompts: List<String> = emptyList(),
    ): Pair<String, List<UniversalMediaItem>> {
        val repo = discoveryRepository ?: return Pair("", emptyList())

        val lastDiscoveryPrompt = findLastDiscoveryPrompt(priorUserPrompts)
        val isFollowUp = isFollowUpRefinement(prompt) && lastDiscoveryPrompt != null
        val effectiveSearchPrompt = if (isFollowUp) {
            "$lastDiscoveryPrompt $prompt"
        } else {
            prompt
        }

        if (!hasDiscoveryIntent(effectiveSearchPrompt)) {
            return Pair("", emptyList())
        }

        return try {
            val filter = resolveFilterFromPrompt(effectiveSearchPrompt)
            var result = repo.discoverUniversal(filter = filter, page = 1)

            // Relax filters if strict discovery yielded nothing
            if ((result !is Result.Success || result.data.isEmpty()) && filter.selectedGenreIds.isNotEmpty()) {
                result = repo.discoverUniversal(
                    filter = filter.copy(selectedGenreIds = emptySet()),
                    page = 1
                )
            }

            // Fallback to title/keyword search if discovery returned empty or prompt is a specific title/franchise
            if (result !is Result.Success || result.data.isEmpty()) {
                val keyword = extractSearchKeyword(effectiveSearchPrompt)
                if (keyword.length >= 2) {
                    val searchResult = repo.searchUniversal(query = keyword)
                    if (searchResult is Result.Success && searchResult.data.isNotEmpty()) {
                        result = searchResult
                    }
                }
            }

            if (result is Result.Success && result.data.isNotEmpty()) {
                val realMovies = result.data.take(4)
                val contextLines = realMovies.joinToString(separator = "\n") { movie ->
                    "- ${movie.title} (${movie.displayYear}, tmdbId: ${movie.id}, rating: ${movie.voteAvg})"
                }
                val followUpContext = if (isFollowUp) {
                    "[Context: The user is refining their previous request (\"$lastDiscoveryPrompt\") with: \"$prompt\". Recommend these matching films:]\n"
                } else {
                    ""
                }
                val referenceText =
                    "\n$followUpContext[Verified ShowTime Films:\n$contextLines\nPresent these films in an a2ui carousel.]"
                Pair(referenceText, realMovies)
            } else {
                Pair("", emptyList())
            }
        } catch (_: Exception) {
            Pair("", emptyList())
        }
    }

    private suspend fun enrichDocumentWithPosters(
        document: A2UiDocument,
        referencedMovies: List<UniversalMediaItem>,
    ): A2UiDocument {
        val repo = discoveryRepository

        suspend fun enrichCard(card: A2UiComponent.MediaCard): A2UiComponent.MediaCard {
            // 1. Try to match from referenced movies first (grounded context)
            val matched = referencedMovies.firstOrNull { ref ->
                (card.tmdbId > 0 && ref.id == card.tmdbId) ||
                        ref.title.equals(card.title, ignoreCase = true) ||
                        ref.title.contains(card.title, ignoreCase = true) ||
                        card.title.contains(ref.title, ignoreCase = true)
            }
            if (matched != null) {
                val realPoster = matched.posterImageUrl.ifBlank { null }
                return card.copy(
                    posterPath = realPoster ?: card.posterPath,
                    tmdbId = if (card.tmdbId <= 0) matched.id else card.tmdbId,
                    releaseYear = if (card.releaseYear.isNullOrBlank()) matched.displayYear else card.releaseYear,
                )
            }

            // 2. If no matched referenced movie, or if the poster is missing/invalid, search TMDB by title
            val currentPoster = card.posterPath
            val isPosterInvalid = currentPoster.isNullOrBlank() ||
                    currentPoster.equals("null", ignoreCase = true) ||
                    currentPoster.equals("n/a", ignoreCase = true) ||
                    !currentPoster.contains("/")

            if (repo != null && (card.tmdbId <= 0 || isPosterInvalid)) {
                try {
                    val searchResult = repo.searchUniversal(query = card.title)
                    if (searchResult is Result.Success && searchResult.data.isNotEmpty()) {
                        val found = searchResult.data.first()
                        val realPoster = found.posterImageUrl.ifBlank { null }
                        return card.copy(
                            posterPath = realPoster ?: card.posterPath,
                            tmdbId = if (card.tmdbId <= 0) found.id else card.tmdbId,
                            releaseYear = if (card.releaseYear.isNullOrBlank()) found.displayYear else card.releaseYear,
                        )
                    }
                } catch (_: Exception) {
                    // Fallback to unchanged card on search failure
                }
            }

            return card
        }

        val enrichedComponents = document.components.map { component ->
            when (component) {
                is A2UiComponent.MediaCarousel -> {
                    val enrichedItems = component.items.map { card ->
                        enrichCard(card)
                    }
                    component.copy(items = enrichedItems)
                }

                is A2UiComponent.MediaCard -> {
                    enrichCard(component)
                }

                else -> component
            }
        }

        return document.copy(components = enrichedComponents)
    }

    fun sendMessage(
        prompt: String,
        sessionId: String = CinephileAgentConfig.SESSION_ID_DEFAULT,
        priorUserPrompts: List<String> = emptyList(),
    ): Flow<CinephileResponseState> = flow {
        val session = getOrCreateSession(sessionId = sessionId)
        val accumulatedText = StringBuilder()
        var currentToolName: String? = null

        emit(CinephileResponseState.Streaming(partialText = "", activeTool = null))

        val (groundingContext, referencedMovies) = fetchCatalogGroundingContext(
            prompt = prompt,
            priorUserPrompts = priorUserPrompts,
        )
        val promptToSend = if (groundingContext.isNotEmpty()) {
            "$prompt\n$groundingContext"
        } else {
            prompt
        }

        session.sendPrompt(prompt = promptToSend).collect { event ->
            when (event) {
                is AiAgentEvent.Token -> {
                    accumulatedText.append(event.token)
                    emit(
                        CinephileResponseState.Streaming(
                            partialText = accumulatedText.toString(),
                            activeTool = currentToolName
                        )
                    )
                }

                is AiAgentEvent.ToolStarted -> {
                    currentToolName = event.toolName
                    emit(
                        CinephileResponseState.Streaming(
                            partialText = accumulatedText.toString(),
                            activeTool = currentToolName
                        )
                    )
                }

                is AiAgentEvent.ToolCompleted -> {
                    currentToolName = null
                    emit(
                        CinephileResponseState.Streaming(
                            partialText = accumulatedText.toString(),
                            activeTool = null
                        )
                    )
                }

                is AiAgentEvent.Completed -> {
                    val parsedDoc = a2UiParser.parse(rawText = event.fullText)
                    val finalDoc = enrichDocumentWithPosters(parsedDoc, referencedMovies)
                    emit(CinephileResponseState.Success(document = finalDoc))
                }

                is AiAgentEvent.Error -> {
                    emit(
                        CinephileResponseState.Failure(
                            message = event.message,
                            isRecoverable = event.isRecoverable,
                        )
                    )
                }
            }
        }
    }

    override fun close() {
        activeSession?.close()
        activeSession = null
    }
}
