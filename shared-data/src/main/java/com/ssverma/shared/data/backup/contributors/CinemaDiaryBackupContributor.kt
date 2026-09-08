package com.ssverma.shared.data.backup.contributors

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ssverma.core.backup.contributor.BackupContributor
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.shared.data.local.db.dao.DiaryDao
import com.ssverma.shared.data.local.db.entity.DiaryEntryEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CinemaDiaryBackupContributor @Inject constructor(
    private val diaryDao: DiaryDao
) : BackupContributor {

    private val gson = Gson()

    override val featureKey: String = BackupMetadata.KEY_DIARY_ENTRIES

    override suspend fun exportData(): JsonElement {
        val entries = diaryDao.getAllDiaryEntriesList()
        return gson.toJsonTree(entries)
    }

    override suspend fun importData(featurePayload: JsonElement?, fullSnapshot: JsonObject) {
        val array = (featurePayload as? JsonArray)
            ?: fullSnapshot.getAsJsonArray(BackupMetadata.KEY_DIARY_ENTRIES)

        if (array != null && !array.isJsonNull && array.size() > 0) {
            val type = object : TypeToken<List<DiaryEntryEntity>>() {}.type
            val items: List<DiaryEntryEntity> = gson.fromJson(array, type) ?: emptyList()
            if (items.isNotEmpty()) {
                diaryDao.insertAll(items)
            }
        }
    }

    override suspend fun getEntityCount(): Int {
        return diaryDao.getAllDiaryEntriesList().size
    }

    override suspend fun getDetailedCounts(): Map<String, Int> {
        return mapOf(BackupMetadata.KEY_DIARY_ENTRIES to getEntityCount())
    }
}
