package com.ssverma.shared.domain.usecase.stats

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.diary.DiaryFilterType
import com.ssverma.shared.domain.model.stats.CinephilePersona
import com.ssverma.shared.domain.model.stats.TasteEraDistribution
import com.ssverma.shared.domain.model.stats.TasteProfileStats
import com.ssverma.shared.domain.model.stats.TasteRatingDistribution
import com.ssverma.shared.domain.repository.DiaryRepository
import com.ssverma.shared.domain.repository.LibraryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject

class GetTasteProfileUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val libraryRepository: LibraryRepository
) {
    operator fun invoke(filter: DiaryFilterType = DiaryFilterType.ALL): Flow<TasteProfileStats> {
        return combine(
            diaryRepository.getAllDiaryEntries(),
            libraryRepository.getAllFavorites(),
            libraryRepository.getAllWatchHistory()
        ) { diaryEntries, _, _ ->
            val filteredEntries = when (filter) {
                DiaryFilterType.ALL -> diaryEntries
                DiaryFilterType.MOVIES_ONLY -> diaryEntries.filter { it.mediaType == MediaType.Movie }
                DiaryFilterType.TV_ONLY -> diaryEntries.filter { it.mediaType == MediaType.Tv }
                DiaryFilterType.REWATCHES_ONLY -> diaryEntries.filter { it.isRewatch }
                DiaryFilterType.FIVE_STARS_ONLY -> diaryEntries.filter { it.userRating >= 5.0f }
            }
            calculateTasteProfile(filteredEntries)
        }
    }

    private fun calculateTasteProfile(
        entries: List<DiaryEntry>
    ): TasteProfileStats {
        if (entries.isEmpty()) {
            return TasteProfileStats()
        }

        val totalItems = entries.size
        val movies = entries.filter { it.mediaType == MediaType.Movie }
        val tvShows = entries.filter { it.mediaType == MediaType.Tv }

        // Approximate runtimes: Movie ~115 mins, TV entry ~45 mins * 8 eps
        val movieMinutes = movies.size * 115
        val tvMinutes = tvShows.size * 45 * 8
        val totalMinutes = movieMinutes + tvMinutes
        val totalHours = totalMinutes / 60
        val totalDays = totalHours / 24f

        val totalRating = entries.sumOf { it.userRating.toDouble() }.toFloat()
        val avgRating = if (totalItems > 0) totalRating / totalItems else 0f

        val rewatchCount = entries.count { it.isRewatch }
        val rewatchPct = if (totalItems > 0) (rewatchCount.toFloat() / totalItems) * 100f else 0f

        // Star rating distribution (5 bands)
        val band5 = entries.count { it.userRating >= 4.5f }
        val band4 = entries.count { it.userRating in 3.5f..<4.5f }
        val band3 = entries.count { it.userRating in 2.5f..<3.5f }
        val band2 = entries.count { it.userRating in 1.5f..<2.5f }
        val band1 = entries.count { it.userRating in 0.5f..<1.5f }

        val ratingDist = listOf(
            TasteRatingDistribution(
                "5 ★",
                5.0f,
                band5,
                if (totalItems > 0) (band5.toFloat() / totalItems) * 100f else 0f
            ),
            TasteRatingDistribution(
                "4 ★",
                4.0f,
                band4,
                if (totalItems > 0) (band4.toFloat() / totalItems) * 100f else 0f
            ),
            TasteRatingDistribution(
                "3 ★",
                3.0f,
                band3,
                if (totalItems > 0) (band3.toFloat() / totalItems) * 100f else 0f
            ),
            TasteRatingDistribution(
                "2 ★",
                2.0f,
                band2,
                if (totalItems > 0) (band2.toFloat() / totalItems) * 100f else 0f
            ),
            TasteRatingDistribution(
                "1 ★",
                1.0f,
                band1,
                if (totalItems > 0) (band1.toFloat() / totalItems) * 100f else 0f
            )
        )

        // Era distribution
        val eras = mutableMapOf<String, Int>()
        entries.forEach { entry ->
            val year = entry.releaseDate.take(4).toIntOrNull()
            val era = when {
                year == null -> "Recent"
                year >= 2020 -> "2020s (Modern)"
                year >= 2010 -> "2010s"
                year >= 2000 -> "2000s"
                year >= 1990 -> "90s Golden"
                else -> "Classics"
            }
            eras[era] = (eras[era] ?: 0) + 1
        }

        val eraDist = eraDistFromMap(eras, totalItems)

        // Top rated seed titles for recommendation anchors
        val topSeeds = entries
            .filter { it.userRating >= 4.0f }
            .map { it.title }
            .distinct()
            .take(5)

        // Cinephile Persona Archetype
        val persona = determinePersona(entries, avgRating, rewatchPct)

        return TasteProfileStats(
            totalWatchedMinutes = totalMinutes,
            totalWatchedHours = totalHours,
            totalWatchedDays = totalDays,
            totalItemsLogged = totalItems,
            totalMoviesLogged = movies.size,
            totalTvLogged = tvShows.size,
            averageRating = (avgRating * 10).toInt() / 10f,
            rewatchCount = rewatchCount,
            rewatchPercentage = (rewatchPct * 10).toInt() / 10f,
            persona = persona,
            ratingDistribution = ratingDist,
            eraDistribution = eraDist,
            topRatedSeedTitles = topSeeds
        )
    }

    private fun eraDistFromMap(
        eras: Map<String, Int>,
        totalItems: Int
    ): List<TasteEraDistribution> {
        return eras.map { (era, count) ->
            TasteEraDistribution(
                eraLabel = era,
                count = count,
                percentage = (count.toFloat() / totalItems) * 100f
            )
        }.sortedByDescending { it.count }
    }

    private fun determinePersona(
        entries: List<DiaryEntry>,
        avgRating: Float,
        rewatchPct: Float
    ): CinephilePersona {
        if (entries.isEmpty()) return CinephilePersona.ECLECTIC_CINEPHILE

        val moviesCount = entries.count { it.mediaType == MediaType.Movie }
        val tvCount = entries.count { it.mediaType == MediaType.Tv }

        return when {
            tvCount > moviesCount -> CinephilePersona.COMEDY_CHAMPION
            avgRating >= 4.2f -> CinephilePersona.DRAMA_DEVOTEE
            rewatchPct >= 40f -> CinephilePersona.ACTION_BUFF
            else -> CinephilePersona.ECLECTIC_CINEPHILE
        }
    }
}
