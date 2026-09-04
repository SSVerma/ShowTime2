package com.ssverma.shared.domain.usecase.stats

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.stats.CinephileMilestone
import com.ssverma.shared.domain.model.stats.CinephileMilestoneDefinition
import com.ssverma.shared.domain.model.stats.MilestoneMetricType
import com.ssverma.shared.domain.model.stats.MilestoneTier
import com.ssverma.shared.domain.model.stats.MonthActivity
import com.ssverma.shared.domain.model.stats.WrappedYearSummary
import com.ssverma.shared.domain.repository.CinephileMilestoneRepository
import com.ssverma.shared.domain.repository.DiaryRepository
import com.ssverma.shared.domain.repository.LibraryRepository
import com.ssverma.shared.domain.utils.DateUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale
import javax.inject.Inject

class GetCinephileWrappedUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository,
    private val libraryRepository: LibraryRepository,
    private val milestoneRepository: CinephileMilestoneRepository
) {
    operator fun invoke(targetYear: Int? = null): Flow<WrappedYearSummary> {
        return combine(
            diaryRepository.getAllDiaryEntries(),
            libraryRepository.getAllFavorites(),
            libraryRepository.getAllWatchlist(),
            milestoneRepository.milestoneDefinitionsFlow
        ) { diaryEntries, favorites, watchlist, milestoneDefs ->
            buildWrappedSummary(
                diaryEntries,
                favorites.size,
                watchlist.size,
                milestoneDefs,
                targetYear
            )
        }
    }

    private fun buildWrappedSummary(
        allEntries: List<DiaryEntry>,
        favoritesCount: Int,
        watchlistCount: Int,
        milestoneDefs: List<CinephileMilestoneDefinition>,
        targetYear: Int?
    ): WrappedYearSummary {
        // Extract distinct available years from diary entries (sorted descending)
        val distinctYears = allEntries.map { entry ->
            DateUtils.fromMillis(entry.loggedAt).year
        }.distinct().sortedDescending()

        val activeYear = targetYear ?: (distinctYears.firstOrNull() ?: DateUtils.currentDate().year)

        val yearEntries = if (activeYear == 0) {
            allEntries
        } else {
            allEntries.filter { entry ->
                DateUtils.fromMillis(entry.loggedAt).year == activeYear
            }
        }

        val yearLabel = if (activeYear == 0) "All-Time" else activeYear.toString()
        val totalLogged = yearEntries.size
        val totalMovies = yearEntries.count { it.mediaType == MediaType.Movie }
        val totalTvShows = yearEntries.count { it.mediaType == MediaType.Tv }

        // Watch time estimations: Movies 115m, TV 45m * 8 eps
        val movieMinutes = totalMovies * 115L
        val tvMinutes = totalTvShows * 45L * 8L
        val totalWatchMinutes = movieMinutes + tvMinutes
        val totalWatchHours = (totalWatchMinutes / 60L).toInt()
        val totalDaysEquivalent = if (totalWatchMinutes > 0) totalWatchMinutes / 1440.0f else 0f

        val averageRating = if (yearEntries.isNotEmpty()) {
            yearEntries.map { it.userRating.toDouble() }.average().toFloat()
        } else 0f

        val rewatchCount = yearEntries.count { it.isRewatch }
        val fiveStarCount = yearEntries.count { it.userRating >= 5.0f }

        // Top 4 rated media (highest user rating first, tie-break by most recent log)
        val topRated = yearEntries
            .sortedWith(compareByDescending<DiaryEntry> { it.userRating }.thenByDescending { it.loggedAt })
            .take(4)

        // Monthly distribution
        val monthCounts = mutableMapOf<Int, Int>()
        for (m in 1..12) monthCounts[m] = 0
        yearEntries.forEach { entry ->
            val month = DateUtils.fromMillis(entry.loggedAt).monthValue
            monthCounts[month] = (monthCounts[month] ?: 0) + 1
        }

        val monthlyDistribution = monthCounts.map { (mIndex, count) ->
            MonthActivity(
                monthName = Month.of(mIndex).getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                monthIndex = mIndex,
                count = count
            )
        }

        val mostActive = monthlyDistribution.filter { it.count > 0 }.maxByOrNull { it.count }

        // Evaluate achievement milestones dynamically from repository definitions
        val milestones = calculateMilestones(
            allEntries = allEntries,
            favoritesCount = favoritesCount,
            watchlistCount = watchlistCount,
            milestoneDefs = milestoneDefs
        )

        return WrappedYearSummary(
            year = activeYear,
            yearLabel = yearLabel,
            totalLogged = totalLogged,
            totalMovies = totalMovies,
            totalTvShows = totalTvShows,
            totalWatchMinutes = totalWatchMinutes,
            totalWatchHours = totalWatchHours,
            totalDaysEquivalent = totalDaysEquivalent,
            averageUserRating = averageRating,
            rewatchCount = rewatchCount,
            fiveStarCount = fiveStarCount,
            topRatedMedia = topRated,
            mostActiveMonth = mostActive,
            monthlyDistribution = monthlyDistribution,
            milestones = milestones,
            availableYears = if (distinctYears.isEmpty()) listOf(DateUtils.currentDate().year) else distinctYears
        )
    }

    private fun calculateMilestones(
        allEntries: List<DiaryEntry>,
        favoritesCount: Int,
        watchlistCount: Int,
        milestoneDefs: List<CinephileMilestoneDefinition>
    ): List<CinephileMilestone> {
        val totalCount = allEntries.size
        val movieCount = allEntries.count { it.mediaType == MediaType.Movie }
        val tvCount = allEntries.count { it.mediaType == MediaType.Tv }
        val fiveStarCount = allEntries.count { it.userRating >= 5.0f }
        val rewatchCount = allEntries.count { it.isRewatch }

        // Total hours calculated from all history
        val allHours = ((movieCount * 115L + tvCount * 45L * 8L) / 60L).toInt()

        // Decade spectrum count
        val distinctDecades = allEntries.mapNotNull { entry ->
            val year = DateUtils.parseIsoDate(entry.releaseDate)?.year
            year?.let { (it / 10) * 10 }
        }.distinct().size

        val curationCount = favoritesCount + watchlistCount

        return milestoneDefs.map { def ->
            val progress = when (def.metricType) {
                MilestoneMetricType.TOTAL_LOGS -> totalCount
                MilestoneMetricType.FIVE_STAR_LOGS -> fiveStarCount
                MilestoneMetricType.REWATCHES -> rewatchCount
                MilestoneMetricType.TOTAL_HOURS -> allHours
                MilestoneMetricType.TV_SHOWS -> tvCount
                MilestoneMetricType.DECADE_COUNT -> distinctDecades
                MilestoneMetricType.CURATION_COUNT -> curationCount
            }

            CinephileMilestone(
                id = def.id,
                title = def.title,
                iconEmoji = def.iconEmoji,
                description = def.description,
                category = def.category,
                tier = def.tier,
                currentProgress = progress.coerceAtMost(def.maxProgress),
                maxProgress = def.maxProgress,
                actionType = def.actionType,
                actionLabel = def.actionLabel
            )
        }
    }
}
