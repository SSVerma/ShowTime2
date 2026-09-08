package com.ssverma.shared.data.backup.contributors

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ssverma.core.backup.contributor.BackupContributor
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.entity.FavoriteEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FavoritesBackupContributor @Inject constructor(
    private val favoriteDao: FavoriteDao
) : BackupContributor {

    private val gson = Gson()

    override val featureKey: String = BackupMetadata.KEY_FAVORITES

    override suspend fun exportData(): JsonElement {
        val favorites = favoriteDao.getAllFavorites()
        return gson.toJsonTree(favorites)
    }

    override suspend fun importData(featurePayload: JsonElement?, fullSnapshot: JsonObject) {
        val array = (featurePayload as? JsonArray)
            ?: fullSnapshot.getAsJsonArray(BackupMetadata.KEY_FAVORITES)

        if (array != null && !array.isJsonNull && array.size() > 0) {
            val type = object : TypeToken<List<FavoriteEntity>>() {}.type
            val items: List<FavoriteEntity> = gson.fromJson(array, type) ?: emptyList()
            if (items.isNotEmpty()) {
                favoriteDao.insertAll(items)
            }
        }
    }

    override suspend fun getEntityCount(): Int {
        return favoriteDao.getAllFavorites().size
    }

    override suspend fun getDetailedCounts(): Map<String, Int> {
        return mapOf(BackupMetadata.KEY_FAVORITES to getEntityCount())
    }
}
