package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.stats.CinephileMilestoneDefinition
import kotlinx.coroutines.flow.Flow

interface CinephileMilestoneRepository {
    val milestoneDefinitionsFlow: Flow<List<CinephileMilestoneDefinition>>
    suspend fun getMilestoneDefinitions(forceRefresh: Boolean = false): List<CinephileMilestoneDefinition>
}
