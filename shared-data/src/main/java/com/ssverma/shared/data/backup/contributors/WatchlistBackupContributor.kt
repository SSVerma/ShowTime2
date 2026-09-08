package com.ssverma.shared.data.backup.contributors

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ssverma.core.backup.contributor.BackupContributor
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.entity.WatchlistEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistBackupContributor @Inject constructor(
    private val watchlistDao: WatchlistDao
) : BackupContributor {

    private val gson = Gson()

    override val featureKey: String = BackupMetadata.KEY_WATCHLIST

    override suspend fun exportData(): JsonElement {
        val watchlist = watchlistDao.getAllWatchlist()
        return gson.toJsonTree(watchlist)
    }

    override suspend fun importData(featurePayload: JsonElement?, fullSnapshot: JsonObject) {
        val array = (featurePayload as? JsonArray)
            ?: fullSnapshot.getAsJsonArray(BackupMetadata.KEY_WATCHLIST)

        if (array != null && !array.isJsonNull && array.size() > 0) {
            val type = object : TypeToken<List<WatchlistEntity>>() {}.type
            val items: List<WatchlistEntity> = gson.fromJson(array, type) ?: emptyList()
            if (items.isNotEmpty()) {
                watchlistDao.insertAll(items)
            }
        }
    }

    override suspend fun getEntityCount(): Int {
        return watchlistDao.getAllWatchlist().size
    }

    override suspend fun getDetailedCounts(): Map<String, Int> {
        return mapOf(BackupMetadata.KEY_WATCHLIST to getEntityCount())
    }
}
