package com.ssverma.shared.domain.model.stats

import com.ssverma.shared.domain.model.diary.DiaryEntry

enum class MilestoneTier {
    BRONZE,
    SILVER,
    GOLD,
    PLATINUM,
    DIAMOND
}

data class CinephileMilestone(
    val id: String,
    val title: String,
    val iconEmoji: String,
    val description: String,
    val category: String,
    val tier: MilestoneTier,
    val currentProgress: Int,
    val maxProgress: Int,
    val isUnlocked: Boolean = currentProgress >= maxProgress,
    val unlockedNote: String? = null
)

data class MonthActivity(
    val monthName: String,
    val monthIndex: Int,
    val count: Int
)

data class WrappedYearSummary(
    val year: Int, // 0 indicates All-Time
    val yearLabel: String, // "2026", "2025", "All-Time"
    val totalLogged: Int,
    val totalMovies: Int,
    val totalTvShows: Int,
    val totalWatchMinutes: Long,
    val totalWatchHours: Int,
    val totalDaysEquivalent: Float,
    val averageUserRating: Float,
    val rewatchCount: Int,
    val fiveStarCount: Int,
    val topRatedMedia: List<DiaryEntry>,
    val mostActiveMonth: MonthActivity?,
    val monthlyDistribution: List<MonthActivity>,
    val milestones: List<CinephileMilestone>,
    val availableYears: List<Int>
)
