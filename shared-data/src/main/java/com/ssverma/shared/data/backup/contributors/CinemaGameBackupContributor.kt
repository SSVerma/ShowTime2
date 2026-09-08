package com.ssverma.shared.data.backup.contributors

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.ssverma.core.backup.contributor.BackupContributor
import com.ssverma.shared.domain.model.game.CinemaGameStats
import com.ssverma.shared.domain.repository.CinemaGameRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CinemaGameBackupContributor @Inject constructor(
    private val cinemaGameRepository: CinemaGameRepository
) : BackupContributor {

    private val gson = Gson()

    override val featureKey: String = FEATURE_KEY

    override suspend fun exportData(): JsonElement {
        val stats = cinemaGameRepository.getGameStats()
        return gson.toJsonTree(stats)
    }

    override suspend fun importData(featurePayload: JsonElement?, fullSnapshot: JsonObject) {
        val statsJson = featurePayload ?: fullSnapshot.get(FEATURE_KEY)
        if (statsJson != null && !statsJson.isJsonNull) {
            val stats = gson.fromJson(statsJson, CinemaGameStats::class.java)
            if (stats != null) {
                cinemaGameRepository.restoreGameStats(stats)
            }
        }
    }

    override suspend fun getEntityCount(): Int {
        return 1
    }

    companion object {
        const val FEATURE_KEY = "gameStats"
    }
}
