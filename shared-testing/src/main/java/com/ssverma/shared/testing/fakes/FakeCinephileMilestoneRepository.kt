package com.ssverma.shared.testing.fakes

import com.ssverma.shared.domain.model.stats.CinephileMilestoneDefinition
import com.ssverma.shared.domain.model.stats.MilestoneActionType
import com.ssverma.shared.domain.model.stats.MilestoneMetricType
import com.ssverma.shared.domain.model.stats.MilestoneTier
import com.ssverma.shared.domain.repository.CinephileMilestoneRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeCinephileMilestoneRepository(
    initialDefinitions: List<CinephileMilestoneDefinition> = defaultMilestoneDefinitions
) : CinephileMilestoneRepository {

    private val definitionsFlow = MutableStateFlow(initialDefinitions)

    override val milestoneDefinitionsFlow: Flow<List<CinephileMilestoneDefinition>> =
        definitionsFlow.asStateFlow()

    override suspend fun getMilestoneDefinitions(forceRefresh: Boolean): List<CinephileMilestoneDefinition> =
        definitionsFlow.value

    fun setDefinitions(definitions: List<CinephileMilestoneDefinition>) {
        definitionsFlow.value = definitions
    }

    companion object {
        val defaultMilestoneDefinitions = listOf(
            CinephileMilestoneDefinition(
                id = "first_reel",
                title = "First Reel",
                iconEmoji = "🎬",
                description = "Log your very first film or series in Cinema Diary",
                category = "Volume",
                tier = MilestoneTier.BRONZE,
                maxProgress = 1,
                metricType = MilestoneMetricType.TOTAL_LOGS,
                actionType = MilestoneActionType.DIARY,
                actionLabel = "Log in Cinema Diary"
            ),
            CinephileMilestoneDefinition(
                id = "silver_explorer",
                title = "Silver Explorer",
                iconEmoji = "🧭",
                description = "Log 25 films or series entries",
                category = "Volume",
                tier = MilestoneTier.SILVER,
                maxProgress = 25,
                metricType = MilestoneMetricType.TOTAL_LOGS,
                actionType = MilestoneActionType.DIARY,
                actionLabel = "Log in Cinema Diary"
            ),
            CinephileMilestoneDefinition(
                id = "century_club",
                title = "Century Club",
                iconEmoji = "🏆",
                description = "Log 100 films or series in your diary",
                category = "Volume",
                tier = MilestoneTier.GOLD,
                maxProgress = 100,
                metricType = MilestoneMetricType.TOTAL_LOGS,
                actionType = MilestoneActionType.DIARY,
                actionLabel = "Log in Cinema Diary"
            ),
            CinephileMilestoneDefinition(
                id = "five_star_connoisseur",
                title = "Five-Star Connoisseur",
                iconEmoji = "⭐",
                description = "Award 10 perfect 5-star ratings",
                category = "Critical Taste",
                tier = MilestoneTier.GOLD,
                maxProgress = 10,
                metricType = MilestoneMetricType.FIVE_STAR_LOGS,
                actionType = MilestoneActionType.DIARY,
                actionLabel = "Log in Cinema Diary"
            ),
            CinephileMilestoneDefinition(
                id = "nostalgia_junkie",
                title = "Nostalgia Junkie",
                iconEmoji = "🔁",
                description = "Log 5 rewatches of comfort classics",
                category = "Dedication",
                tier = MilestoneTier.BRONZE,
                maxProgress = 5,
                metricType = MilestoneMetricType.REWATCHES,
                actionType = MilestoneActionType.DIARY,
                actionLabel = "Log a Rewatch"
            ),
            CinephileMilestoneDefinition(
                id = "marathon_master",
                title = "Marathon Master",
                iconEmoji = "⏱️",
                description = "Accumulate 100 hours of viewing time",
                category = "Endurance",
                tier = MilestoneTier.GOLD,
                maxProgress = 100,
                metricType = MilestoneMetricType.TOTAL_HOURS,
                actionType = MilestoneActionType.DIARY,
                actionLabel = "Log in Cinema Diary"
            ),
            CinephileMilestoneDefinition(
                id = "binge_overlord",
                title = "Binge Overlord",
                iconEmoji = "📺",
                description = "Log 25 television series seasons or entries",
                category = "Television",
                tier = MilestoneTier.SILVER,
                maxProgress = 25,
                metricType = MilestoneMetricType.TV_SHOWS,
                actionType = MilestoneActionType.DISCOVERY,
                actionLabel = "Discover TV Shows"
            ),
            CinephileMilestoneDefinition(
                id = "decade_hopper",
                title = "Decade Hopper",
                iconEmoji = "⏳",
                description = "Log films released across 5 different decades",
                category = "Diversity",
                tier = MilestoneTier.PLATINUM,
                maxProgress = 5,
                metricType = MilestoneMetricType.DECADE_COUNT,
                actionType = MilestoneActionType.DISCOVERY,
                actionLabel = "Explore Timeless Cinema"
            ),
            CinephileMilestoneDefinition(
                id = "curator_pro",
                title = "Curator Pro",
                iconEmoji = "💎",
                description = "Curate 3 personal cinema lists",
                category = "Curation",
                tier = MilestoneTier.DIAMOND,
                maxProgress = 3,
                metricType = MilestoneMetricType.CURATION_COUNT,
                actionType = MilestoneActionType.TASTE_PROFILE,
                actionLabel = "View Taste Profile"
            )
        )
    }
}
