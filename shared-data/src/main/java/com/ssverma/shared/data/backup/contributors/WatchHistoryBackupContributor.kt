package com.ssverma.shared.data.backup.contributors

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ssverma.core.backup.contributor.BackupContributor
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.entity.WatchHistoryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchHistoryBackupContributor @Inject constructor(
    private val watchHistoryDao: WatchHistoryDao
) : BackupContributor {

    private val gson = Gson()

    override val featureKey: String = BackupMetadata.KEY_HISTORY

    override suspend fun exportData(): JsonElement {
        val history = watchHistoryDao.getAllHistory()
        return gson.toJsonTree(history)
    }

    override suspend fun importData(featurePayload: JsonElement?, fullSnapshot: JsonObject) {
        val array = (featurePayload as? JsonArray)
            ?: fullSnapshot.getAsJsonArray(BackupMetadata.KEY_HISTORY)

        if (array != null && !array.isJsonNull && array.size() > 0) {
            val type = object : TypeToken<List<WatchHistoryEntity>>() {}.type
            val items: List<WatchHistoryEntity> = gson.fromJson(array, type) ?: emptyList()
            if (items.isNotEmpty()) {
                watchHistoryDao.insertAll(items)
            }
        }
    }

    override suspend fun getEntityCount(): Int {
        return watchHistoryDao.getAllHistory().size
    }

    override suspend fun getDetailedCounts(): Map<String, Int> {
        return mapOf(BackupMetadata.KEY_HISTORY to getEntityCount())
    }
}
