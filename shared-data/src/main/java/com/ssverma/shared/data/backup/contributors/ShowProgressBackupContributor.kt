package com.ssverma.shared.data.backup.contributors

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ssverma.core.backup.contributor.BackupContributor
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.shared.data.local.db.dao.EpisodeWatchHistoryDao
import com.ssverma.shared.data.local.db.dao.ShowWatchProgressDao
import com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShowProgressBackupContributor @Inject constructor(
    private val showWatchProgressDao: ShowWatchProgressDao,
    private val episodeWatchHistoryDao: EpisodeWatchHistoryDao
) : BackupContributor {

    private val gson = Gson()

    override val featureKey: String = BackupMetadata.KEY_SHOW_PROGRESS

    override suspend fun exportData(): JsonElement {
        val progress = showWatchProgressDao.getAllProgress()
        val history = episodeWatchHistoryDao.getAllHistory()
        val obj = JsonObject()
        obj.add(BackupMetadata.KEY_SHOW_PROGRESS, gson.toJsonTree(progress))
        obj.add(BackupMetadata.KEY_EPISODE_HISTORY, gson.toJsonTree(history))
        return obj
    }

    override suspend fun importData(featurePayload: JsonElement?, fullSnapshot: JsonObject) {
        val payloadObj = featurePayload as? JsonObject

        val progressArray = payloadObj?.getAsJsonArray(BackupMetadata.KEY_SHOW_PROGRESS)
            ?: fullSnapshot.getAsJsonArray(BackupMetadata.KEY_SHOW_PROGRESS)

        val historyArray = payloadObj?.getAsJsonArray(BackupMetadata.KEY_EPISODE_HISTORY)
            ?: fullSnapshot.getAsJsonArray(BackupMetadata.KEY_EPISODE_HISTORY)

        if (progressArray != null && !progressArray.isJsonNull && progressArray.size() > 0) {
            val progressType = object : TypeToken<List<ShowWatchProgressEntity>>() {}.type
            val items: List<ShowWatchProgressEntity> =
                gson.fromJson(progressArray, progressType) ?: emptyList()
            if (items.isNotEmpty()) {
                showWatchProgressDao.insertAll(items)
            }
        }

        if (historyArray != null && !historyArray.isJsonNull && historyArray.size() > 0) {
            val historyType = object : TypeToken<List<EpisodeWatchHistoryEntity>>() {}.type
            val items: List<EpisodeWatchHistoryEntity> =
                gson.fromJson(historyArray, historyType) ?: emptyList()
            if (items.isNotEmpty()) {
                episodeWatchHistoryDao.insertAll(items)
            }
        }
    }

    override suspend fun getEntityCount(): Int {
        return showWatchProgressDao.getAllProgress().size + episodeWatchHistoryDao.getAllHistory().size
    }

    override suspend fun getDetailedCounts(): Map<String, Int> {
        return mapOf(
            BackupMetadata.KEY_SHOW_PROGRESS to showWatchProgressDao.getAllProgress().size,
            BackupMetadata.KEY_EPISODE_HISTORY to episodeWatchHistoryDao.getAllHistory().size
        )
    }
}
