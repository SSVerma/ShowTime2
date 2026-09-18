package com.ssverma.shared.domain.model.stats

import com.google.gson.annotations.SerializedName
import com.ssverma.shared.domain.model.diary.DiaryEntry

enum class MilestoneTier {
    @SerializedName("BRONZE")
    BRONZE,

    @SerializedName("SILVER")
    SILVER,

    @SerializedName("GOLD")
    GOLD,

    @SerializedName("PLATINUM")
    PLATINUM,

    @SerializedName("DIAMOND")
    DIAMOND
}

enum class MilestoneActionType {
    @SerializedName("DIARY")
    DIARY,

    @SerializedName("DISCOVERY")
    DISCOVERY,

    @SerializedName("TASTE_PROFILE")
    TASTE_PROFILE
}

enum class MilestoneMetricType {
    @SerializedName("TOTAL_LOGS")
    TOTAL_LOGS,

    @SerializedName("FIVE_STAR_LOGS")
    FIVE_STAR_LOGS,

    @SerializedName("REWATCHES")
    REWATCHES,

    @SerializedName("TOTAL_HOURS")
    TOTAL_HOURS,

    @SerializedName("TV_SHOWS")
    TV_SHOWS,

    @SerializedName("DECADE_COUNT")
    DECADE_COUNT,

    @SerializedName("CURATION_COUNT")
    CURATION_COUNT
}

data class CinephileMilestoneDefinition(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("iconEmoji")
    val iconEmoji: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("tier")
    val tier: MilestoneTier,
    @SerializedName("maxProgress")
    val maxProgress: Int,
    @SerializedName("metricType")
    val metricType: MilestoneMetricType,
    @SerializedName("actionType")
    val actionType: MilestoneActionType = MilestoneActionType.DIARY,
    @SerializedName("actionLabel")
    val actionLabel: String = "Log in Cinema Diary"
)

data class CinephileMilestone(
    @SerializedName("id")
    val id: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("iconEmoji")
    val iconEmoji: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("category")
    val category: String,
    @SerializedName("tier")
    val tier: MilestoneTier,
    @SerializedName("currentProgress")
    val currentProgress: Int,
    @SerializedName("maxProgress")
    val maxProgress: Int,
    @SerializedName("isUnlocked")
    val isUnlocked: Boolean = currentProgress >= maxProgress,
    @SerializedName("unlockedNote")
    val unlockedNote: String? = null,
    @SerializedName("actionType")
    val actionType: MilestoneActionType = MilestoneActionType.DIARY,
    @SerializedName("actionLabel")
    val actionLabel: String = "Log in Cinema Diary"
) {
    val remainingProgress: Int get() = (maxProgress - currentProgress).coerceAtLeast(0)
    val progressPercentage: Float
        get() = if (maxProgress > 0) (currentProgress.toFloat() / maxProgress.toFloat()).coerceIn(
            0f,
            1f
        ) else 0f
}

data class MonthActivity(
    @SerializedName("monthName")
    val monthName: String,
    @SerializedName("monthIndex")
    val monthIndex: Int,
    @SerializedName("count")
    val count: Int
)

data class WrappedYearSummary(
    @SerializedName("year")
    val year: Int, // 0 indicates All-Time
    @SerializedName("yearLabel")
    val yearLabel: String, // "2026", "2025", "All-Time"
    @SerializedName("totalLogged")
    val totalLogged: Int,
    @SerializedName("totalMovies")
    val totalMovies: Int,
    @SerializedName("totalTvShows")
    val totalTvShows: Int,
    @SerializedName("totalWatchMinutes")
    val totalWatchMinutes: Long,
    @SerializedName("totalWatchHours")
    val totalWatchHours: Int,
    @SerializedName("totalDaysEquivalent")
    val totalDaysEquivalent: Float,
    @SerializedName("averageUserRating")
    val averageUserRating: Float,
    @SerializedName("rewatchCount")
    val rewatchCount: Int,
    @SerializedName("fiveStarCount")
    val fiveStarCount: Int,
    @SerializedName("topRatedMedia")
    val topRatedMedia: List<DiaryEntry>,
    @SerializedName("mostActiveMonth")
    val mostActiveMonth: MonthActivity?,
    @SerializedName("monthlyDistribution")
    val monthlyDistribution: List<MonthActivity>,
    @SerializedName("milestones")
    val milestones: List<CinephileMilestone>,
    @SerializedName("availableYears")
    val availableYears: List<Int>
)
