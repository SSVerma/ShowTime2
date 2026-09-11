package com.ssverma.shared.data.backup.contributors

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ssverma.core.backup.contributor.BackupContributor
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.shared.data.local.adapter.MediaTypeJsonAdapter
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.challenge.BlindspotPriorityItem
import com.ssverma.shared.domain.model.challenge.CinephileChallenge
import com.ssverma.shared.domain.repository.BacklogRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BacklogBackupContributor @Inject constructor(
    private val backlogRepository: BacklogRepository
) : BackupContributor {

    private val gson: Gson = GsonBuilder()
        .registerTypeAdapter(MediaType::class.java, MediaTypeJsonAdapter())
        .create()

    override val featureKey: String = FEATURE_KEY

    override suspend fun exportData(): JsonElement {
        val challenges = backlogRepository.activeChallengesFlow.first()
        val blindspots = backlogRepository.blindspotsFlow.first()

        val obj = JsonObject()
        obj.add(KEY_ACTIVE_CHALLENGES, gson.toJsonTree(challenges))
        obj.add(KEY_BLINDSPOTS, gson.toJsonTree(blindspots))
        return obj
    }

    override suspend fun importData(featurePayload: JsonElement?, fullSnapshot: JsonObject) {
        val payloadObj = featurePayload as? JsonObject

        val challengesJson = payloadObj?.get(KEY_ACTIVE_CHALLENGES)
            ?: fullSnapshot.get(KEY_ACTIVE_CHALLENGES)

        val blindspotsJson = payloadObj?.get(KEY_BLINDSPOTS)
            ?: fullSnapshot.get(KEY_BLINDSPOTS)

        val challenges: List<CinephileChallenge> =
            if (challengesJson != null && !challengesJson.isJsonNull) {
                val type = object : TypeToken<List<CinephileChallenge>>() {}.type
                gson.fromJson(challengesJson, type) ?: emptyList()
            } else {
                emptyList()
            }

        val blindspots: List<BlindspotPriorityItem> =
            if (blindspotsJson != null && !blindspotsJson.isJsonNull) {
                val type = object : TypeToken<List<BlindspotPriorityItem>>() {}.type
                gson.fromJson(blindspotsJson, type) ?: emptyList()
            } else {
                emptyList()
            }

        if (challenges.isNotEmpty() || blindspots.isNotEmpty()) {
            backlogRepository.restoreBacklog(challenges, blindspots)
        }
    }

    override suspend fun getEntityCount(): Int {
        val challenges = backlogRepository.activeChallengesFlow.first().size
        val blindspots = backlogRepository.blindspotsFlow.first().size
        return challenges + blindspots
    }

    override suspend fun getDetailedCounts(): Map<String, Int> {
        val challenges = backlogRepository.activeChallengesFlow.first().size
        val blindspots = backlogRepository.blindspotsFlow.first().size
        return mapOf(
            BackupMetadata.KEY_CHALLENGES to challenges,
            BackupMetadata.KEY_BLINDSPOTS to blindspots
        )
    }

    companion object {
        const val FEATURE_KEY = "backlog"
        const val KEY_ACTIVE_CHALLENGES = "activeChallenges"
        const val KEY_BLINDSPOTS = "blindspots"
    }
}
