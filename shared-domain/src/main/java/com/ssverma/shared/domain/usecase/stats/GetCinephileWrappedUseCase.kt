package com.ssverma.shared.domain.usecase.stats

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.stats.CinephileMilestone
import com.ssverma.shared.domain.model.stats.MilestoneTier
import com.ssverma.shared.domain.model.stats.MonthActivity
import com.ssverma.shared.domain.model.stats.WrappedYearSummary
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
    private val libraryRepository: LibraryRepository
) {
    operator fun invoke(targetYear: Int? = null): Flow<WrappedYearSummary> {
        return combine(
            diaryRepository.getAllDiaryEntries(),
            libraryRepository.getAllFavorites(),
            libraryRepository.getAllWatchlist()
        ) { diaryEntries, favorites, watchlist ->
            buildWrappedSummary(diaryEntries, favorites.size, watchlist.size, targetYear)
        }
    }

    private fun buildWrappedSummary(
        allEntries: List<DiaryEntry>,
        favoritesCount: Int,
        watchlistCount: Int,
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

        // Evaluate achievement milestones
        val milestones = calculateMilestones(
            allEntries = allEntries,
            favoritesCount = favoritesCount,
            watchlistCount = watchlistCount
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
        watchlistCount: Int
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

        return listOf(
            CinephileMilestone(
                id = "first_reel",
                title = "First Reel",
                iconEmoji = "🎬",
                description = "Logged your first cinema diary entry",
                category = "General",
                tier = MilestoneTier.BRONZE,
                currentProgress = totalCount.coerceAtMost(1),
                maxProgress = 1
            ),
            CinephileMilestone(
                id = "silver_explorer",
                title = "Silver Screen Explorer",
                iconEmoji = "🥈",
                description = "Logged 25 movies or TV shows",
                category = "Volume",
                tier = MilestoneTier.SILVER,
                currentProgress = totalCount.coerceAtMost(25),
                maxProgress = 25
            ),
            CinephileMilestone(
                id = "century_club",
                title = "Century of Cinema",
                iconEmoji = "🥇",
                description = "Logged 100 movies or TV shows",
                category = "Volume",
                tier = MilestoneTier.GOLD,
                currentProgress = totalCount.coerceAtMost(100),
                maxProgress = 100
            ),
            CinephileMilestone(
                id = "cinephile_legend",
                title = "Cinephile Legend",
                iconEmoji = "👑",
                description = "Logged 250+ titles in your diary",
                category = "Volume",
                tier = MilestoneTier.DIAMOND,
                currentProgress = totalCount.coerceAtMost(250),
                maxProgress = 250
            ),
            CinephileMilestone(
                id = "five_star_connoisseur",
                title = "Five-Star Connoisseur",
                iconEmoji = "🌟",
                description = "Awarded 10 masterclass 5-star ratings",
                category = "Critical Taste",
                tier = MilestoneTier.GOLD,
                currentProgress = fiveStarCount.coerceAtMost(10),
                maxProgress = 10
            ),
            CinephileMilestone(
                id = "nostalgia_junkie",
                title = "Nostalgia Junkie",
                iconEmoji = "🔁",
                description = "Logged 5 comforting rewatches",
                category = "Dedication",
                tier = MilestoneTier.BRONZE,
                currentProgress = rewatchCount.coerceAtMost(5),
                maxProgress = 5
            ),
            CinephileMilestone(
                id = "marathon_master",
                title = "Marathon Master",
                iconEmoji = "⏱️",
                description = "Completed 50+ total hours of screen time",
                category = "Endurance",
                tier = MilestoneTier.SILVER,
                currentProgress = allHours.coerceAtMost(50),
                maxProgress = 50
            ),
            CinephileMilestone(
                id = "binge_overlord",
                title = "TV Binge Overlord",
                iconEmoji = "📺",
                description = "Logged 10+ TV shows in your diary",
                category = "Television",
                tier = MilestoneTier.SILVER,
                currentProgress = tvCount.coerceAtMost(10),
                maxProgress = 10
            ),
            CinephileMilestone(
                id = "decade_hopper",
                title = "Decade Hopper",
                iconEmoji = "🎞️",
                description = "Watched media spanning 4 distinct decades",
                category = "Heritage",
                tier = MilestoneTier.PLATINUM,
                currentProgress = distinctDecades.coerceAtMost(4),
                maxProgress = 4
            ),
            CinephileMilestone(
                id = "curator_pro",
                title = "Vault Curator",
                iconEmoji = "💎",
                description = "Added 15+ titles to your watchlist or favorites",
                category = "Curation",
                tier = MilestoneTier.BRONZE,
                currentProgress = (favoritesCount + watchlistCount).coerceAtMost(15),
                maxProgress = 15
            )
        )
    }
}
