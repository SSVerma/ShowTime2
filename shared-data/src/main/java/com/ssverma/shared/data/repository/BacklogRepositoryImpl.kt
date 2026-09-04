package com.ssverma.shared.data.repository

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import com.ssverma.shared.data.local.adapter.MediaTypeJsonAdapter
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.BlindspotPriorityItem
import com.ssverma.shared.domain.model.challenge.ChallengeCategory
import com.ssverma.shared.domain.model.challenge.ChallengeMediaItem
import com.ssverma.shared.domain.model.challenge.ChallengeMediaTypeFilter
import com.ssverma.shared.domain.model.challenge.CinephileChallenge
import com.ssverma.shared.domain.repository.BacklogRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BacklogRepositoryImpl @Inject constructor(
    @param:ApplicationContext private val context: Context,
    keyValueStorageClient: KeyValueStorageClient
) : BacklogRepository {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(MediaType::class.java, MediaTypeJsonAdapter())
        .create()

    private val storage: KeyValueStorage = keyValueStorageClient.createKeyValueStorage(
        context = context,
        config = KeyValueStorageConfig(fileName = "showtime_backlog_challenges_prefs")
    )

    private companion object {
        val KEY_ACTIVE_CHALLENGES = stringPreferencesKey("backlog_active_challenges_json")
        val KEY_BLINDSPOTS = stringPreferencesKey("backlog_blindspots_json")
    }

    override val activeChallengesFlow: Flow<List<CinephileChallenge>> = storage.data.map { prefs ->
        val json = prefs[KEY_ACTIVE_CHALLENGES]
        if (json.isNullOrBlank()) {
            // Default: start with the top curated challenge joined
            val defaultList = listOf(getCuratedChallenges().first())
            defaultList
        } else {
            try {
                val type = object : TypeToken<List<CinephileChallenge>>() {}.type
                gson.fromJson<List<CinephileChallenge>>(json, type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    override val blindspotsFlow: Flow<List<BlindspotPriorityItem>> = storage.data.map { prefs ->
        val json = prefs[KEY_BLINDSPOTS]
        if (json.isNullOrBlank()) {
            emptyList()
        } else {
            try {
                val type = object : TypeToken<List<BlindspotPriorityItem>>() {}.type
                gson.fromJson<List<BlindspotPriorityItem>>(json, type) ?: emptyList()
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    override suspend fun getCuratedChallenges(): List<CinephileChallenge> {
        return listOf(
            CinephileChallenge(
                id = "curated_sight_and_sound",
                title = "Sight & Sound Masterpieces",
                description = "The defining cinematic milestones of world cinema history.",
                category = ChallengeCategory.Curated,
                mediaTypeFilter = ChallengeMediaTypeFilter.MOVIE,
                targetCount = 10,
                targetMediaItems = listOf(
                    ChallengeMediaItem(
                        id = 238,
                        title = "The Godfather",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/3bhkrj58Vtu7enYsRolD1fZdja1.jpg",
                        releaseYear = "1972",
                        directorOrCreator = "Francis Ford Coppola",
                        voteAvg = 8.7f
                    ),
                    ChallengeMediaItem(
                        id = 62,
                        title = "2001: A Space Odyssey",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/90T7b2LIrL07ndYSaYr0BDkeYnu.jpg",
                        releaseYear = "1968",
                        directorOrCreator = "Stanley Kubrick",
                        voteAvg = 8.1f
                    ),
                    ChallengeMediaItem(
                        id = 429,
                        title = "The Good, the Bad and the Ugly",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/bX2xnavhMYjWDoZp1VM6VnU1xwe.jpg",
                        releaseYear = "1966",
                        directorOrCreator = "Sergio Leone",
                        voteAvg = 8.5f
                    ),
                    ChallengeMediaItem(
                        id = 129,
                        title = "Spirited Away",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/39wmItIWsg5sZMyRUHLkWBcuVCM.jpg",
                        releaseYear = "2001",
                        directorOrCreator = "Hayao Miyazaki",
                        voteAvg = 8.5f
                    ),
                    ChallengeMediaItem(
                        id = 389,
                        title = "12 Angry Men",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/ow3wq89wM8qd5X7hWKxiRfsFf9C.jpg",
                        releaseYear = "1957",
                        directorOrCreator = "Sidney Lumet",
                        voteAvg = 8.5f
                    ),
                    ChallengeMediaItem(
                        id = 497,
                        title = "Schindler's List",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/sF1U4EUQS8YHUYjNl3pMGNIQyr0.jpg",
                        releaseYear = "1993",
                        directorOrCreator = "Steven Spielberg",
                        voteAvg = 8.6f
                    ),
                    ChallengeMediaItem(
                        id = 155,
                        title = "The Dark Knight",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/qJ2tW6WMUDux911r6m7haRef0WH.jpg",
                        releaseYear = "2008",
                        directorOrCreator = "Christopher Nolan",
                        voteAvg = 8.5f
                    ),
                    ChallengeMediaItem(
                        id = 496243,
                        title = "Parasite",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/7IiTTgloJzvGI1TAYymCfbfl3vT.jpg",
                        releaseYear = "2019",
                        directorOrCreator = "Bong Joon-ho",
                        voteAvg = 8.5f
                    ),
                    ChallengeMediaItem(
                        id = 19404,
                        title = "Dilwale Dulhania Le Jayenge",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/ktejodb09GplUm0nR1zM447uqjY.jpg",
                        releaseYear = "1995",
                        directorOrCreator = "Aditya Chopra",
                        voteAvg = 8.5f
                    ),
                    ChallengeMediaItem(
                        id = 157336,
                        title = "Interstellar",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/gEU2QniE6E77NI6lCU6MxlNBvIx.jpg",
                        releaseYear = "2014",
                        directorOrCreator = "Christopher Nolan",
                        voteAvg = 8.4f
                    )
                )
            ),
            CinephileChallenge(
                id = "curated_prestige_tv",
                title = "Prestige TV Hall of Fame",
                description = "The gold standard of serialized storytelling that redefined television.",
                category = ChallengeCategory.Curated,
                mediaTypeFilter = ChallengeMediaTypeFilter.TV,
                targetCount = 8,
                targetMediaItems = listOf(
                    ChallengeMediaItem(
                        id = 1396,
                        title = "Breaking Bad",
                        mediaType = MediaType.Tv,
                        posterImageUrl = "/ztkUQFLlC19CCMYHW9o1zWhJRNq.jpg",
                        releaseYear = "2008",
                        directorOrCreator = "Vince Gilligan",
                        voteAvg = 8.9f
                    ),
                    ChallengeMediaItem(
                        id = 76331,
                        title = "Succession",
                        mediaType = MediaType.Tv,
                        posterImageUrl = "/7TB1B6wX5jT1h1fGq4T5vN0u1s2.jpg",
                        releaseYear = "2018",
                        directorOrCreator = "Jesse Armstrong",
                        voteAvg = 8.6f
                    ),
                    ChallengeMediaItem(
                        id = 87108,
                        title = "Chernobyl",
                        mediaType = MediaType.Tv,
                        posterImageUrl = "/hlLXt2tOPT6RRnjiUmoxyG1LTFi.jpg",
                        releaseYear = "2019",
                        directorOrCreator = "Craig Mazin",
                        voteAvg = 8.7f
                    ),
                    ChallengeMediaItem(
                        id = 1398,
                        title = "The Sopranos",
                        mediaType = MediaType.Tv,
                        posterImageUrl = "/rweIrveL43TaxUN0akQEaAXL6x0.jpg",
                        releaseYear = "1999",
                        directorOrCreator = "David Chase",
                        voteAvg = 8.6f
                    ),
                    ChallengeMediaItem(
                        id = 1438,
                        title = "The Wire",
                        mediaType = MediaType.Tv,
                        posterImageUrl = "/og7Glq4HGe1uPebG2M9kC0oTsmF.jpg",
                        releaseYear = "2002",
                        directorOrCreator = "David Simon",
                        voteAvg = 8.6f
                    ),
                    ChallengeMediaItem(
                        id = 60059,
                        title = "Better Call Saul",
                        mediaType = MediaType.Tv,
                        posterImageUrl = "/fC2HDm5t0kHsfNxPkUQIZIMhv1X.jpg",
                        releaseYear = "2015",
                        directorOrCreator = "Peter Gould",
                        voteAvg = 8.7f
                    ),
                    ChallengeMediaItem(
                        id = 100088,
                        title = "The Last of Us",
                        mediaType = MediaType.Tv,
                        posterImageUrl = "/uKvVjHNqB5VmOrdxqAt2V7JMrne.jpg",
                        releaseYear = "2023",
                        directorOrCreator = "Craig Mazin, Neil Druckmann",
                        voteAvg = 8.6f
                    ),
                    ChallengeMediaItem(
                        id = 94997,
                        title = "House of the Dragon",
                        mediaType = MediaType.Tv,
                        posterImageUrl = "/t9Xke5724fqtp0dL4DR29YmKdua.jpg",
                        releaseYear = "2022",
                        directorOrCreator = "Ryan J. Condal",
                        voteAvg = 8.4f
                    )
                )
            ),
            CinephileChallenge(
                id = "curated_nolan_villeneuve",
                title = "Auteur Vision: Nolan & Villeneuve",
                description = "Modern visionary epics blending grand spectacle with existential depth.",
                category = ChallengeCategory.DirectorSpotlight,
                mediaTypeFilter = ChallengeMediaTypeFilter.MOVIE,
                targetCount = 6,
                targetMediaItems = listOf(
                    ChallengeMediaItem(
                        id = 872585,
                        title = "Oppenheimer",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/8Gxv8gSFCU0XGDykEGv7zR1n2ua.jpg",
                        releaseYear = "2023",
                        directorOrCreator = "Christopher Nolan",
                        voteAvg = 8.1f
                    ),
                    ChallengeMediaItem(
                        id = 693134,
                        title = "Dune: Part Two",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/1pdfLvkbY9ohJlCjQH2CZjjYVvJ.jpg",
                        releaseYear = "2024",
                        directorOrCreator = "Denis Villeneuve",
                        voteAvg = 8.2f
                    ),
                    ChallengeMediaItem(
                        id = 27205,
                        title = "Inception",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/edv5CZvWj09upOsy2Y6IwDhK8bt.jpg",
                        releaseYear = "2010",
                        directorOrCreator = "Christopher Nolan",
                        voteAvg = 8.4f
                    ),
                    ChallengeMediaItem(
                        id = 335984,
                        title = "Blade Runner 2049",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/gajva2L0rPYkEWjzgFlBXCAVBE5.jpg",
                        releaseYear = "2017",
                        directorOrCreator = "Denis Villeneuve",
                        voteAvg = 7.6f
                    ),
                    ChallengeMediaItem(
                        id = 329865,
                        title = "Arrival",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/x2FJsf1ElAgr63Y3PNPtJrcmpoe.jpg",
                        releaseYear = "2016",
                        directorOrCreator = "Denis Villeneuve",
                        voteAvg = 7.9f
                    ),
                    ChallengeMediaItem(
                        id = 146233,
                        title = "Prisoners",
                        mediaType = MediaType.Movie,
                        posterImageUrl = "/jsW6cpm0t2Wd91a92B60w5xQo4H.jpg",
                        releaseYear = "2013",
                        directorOrCreator = "Denis Villeneuve",
                        voteAvg = 8.1f
                    )
                )
            ),
            CinephileChallenge(
                id = "curated_century_sprint_2026",
                title = "2026 Cinema Century Sprint",
                description = "Watch 52 Movies or TV Shows this year (1 title per week).",
                category = ChallengeCategory.PersonalGoal,
                mediaTypeFilter = ChallengeMediaTypeFilter.ALL,
                targetCount = 52,
                targetMediaItems = emptyList()
            )
        )
    }

    override suspend fun joinChallenge(challenge: CinephileChallenge): Unit =
        withContext(Dispatchers.IO) {
            val current = activeChallengesFlow.first().toMutableList()
            if (current.none { it.id == challenge.id }) {
                val joined = challenge.copy(joinedAt = System.currentTimeMillis())
                current.add(joined)
                storage.edit { prefs ->
                    prefs[KEY_ACTIVE_CHALLENGES] = gson.toJson(current)
                }
            }
        }

    override suspend fun leaveChallenge(challengeId: String): Unit = withContext(Dispatchers.IO) {
        val current = activeChallengesFlow.first().toMutableList()
        current.removeAll { it.id == challengeId }
        storage.edit { prefs ->
            prefs[KEY_ACTIVE_CHALLENGES] = gson.toJson(current)
        }
    }

    override suspend fun createCustomChallenge(
        title: String,
        description: String,
        mediaTypeFilter: ChallengeMediaTypeFilter,
        targetCount: Int,
        targetItems: List<ChallengeMediaItem>
    ): CinephileChallenge = withContext(Dispatchers.IO) {
        val newChallenge = CinephileChallenge(
            id = "custom_${UUID.randomUUID()}",
            title = title,
            description = description,
            category = ChallengeCategory.PersonalGoal,
            mediaTypeFilter = mediaTypeFilter,
            targetCount = maxOf(targetCount, targetItems.size, 1),
            targetMediaItems = targetItems,
            isCustom = true,
            joinedAt = System.currentTimeMillis()
        )
        joinChallenge(newChallenge)
        newChallenge
    }

    override suspend fun deleteCustomChallenge(challengeId: String): Unit =
        withContext(Dispatchers.IO) {
            leaveChallenge(challengeId)
        }

    override suspend fun addBlindspot(item: BlindspotPriorityItem): Unit =
        withContext(Dispatchers.IO) {
            val current = blindspotsFlow.first().toMutableList()
            current.removeAll { it.mediaId == item.mediaId && it.mediaType == item.mediaType }
            current.add(0, item)
            storage.edit { prefs ->
                prefs[KEY_BLINDSPOTS] = gson.toJson(current)
            }
        }

    override suspend fun removeBlindspot(mediaId: Int, mediaType: MediaType): Unit =
        withContext(Dispatchers.IO) {
            val current = blindspotsFlow.first().toMutableList()
            current.removeAll { it.mediaId == mediaId && it.mediaType == mediaType }
            storage.edit { prefs ->
                prefs[KEY_BLINDSPOTS] = gson.toJson(current)
            }
        }

    override suspend fun isBlindspot(mediaId: Int, mediaType: MediaType): Boolean =
        withContext(Dispatchers.IO) {
            val current = blindspotsFlow.first()
            current.any { it.mediaId == mediaId && it.mediaType == mediaType }
        }
}
