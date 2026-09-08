package com.ssverma.shared.data.backup.contributors

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.ssverma.core.backup.contributor.BackupContributor
import com.ssverma.core.backup.model.BackupMetadata
import com.ssverma.shared.data.local.db.dao.CustomListDao
import com.ssverma.shared.data.local.db.entity.CustomListEntity
import com.ssverma.shared.data.local.db.entity.CustomListItemEntity
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomListsBackupContributor @Inject constructor(
    private val customListDao: CustomListDao
) : BackupContributor {

    private val gson = Gson()

    override val featureKey: String = BackupMetadata.KEY_CUSTOM_LISTS

    override suspend fun exportData(): JsonElement {
        val lists = customListDao.getAllLists()
        val items = customListDao.getAllListItems()
        val obj = JsonObject()
        obj.add(BackupMetadata.KEY_CUSTOM_LISTS, gson.toJsonTree(lists))
        obj.add(BackupMetadata.KEY_CUSTOM_LIST_ITEMS, gson.toJsonTree(items))
        return obj
    }

    override suspend fun importData(featurePayload: JsonElement?, fullSnapshot: JsonObject) {
        val payloadObj = featurePayload as? JsonObject

        val listsArray = payloadObj?.getAsJsonArray(BackupMetadata.KEY_CUSTOM_LISTS)
            ?: fullSnapshot.getAsJsonArray(BackupMetadata.KEY_CUSTOM_LISTS)

        val itemsArray = payloadObj?.getAsJsonArray(BackupMetadata.KEY_CUSTOM_LIST_ITEMS)
            ?: fullSnapshot.getAsJsonArray(BackupMetadata.KEY_CUSTOM_LIST_ITEMS)

        if (listsArray != null && !listsArray.isJsonNull && listsArray.size() > 0) {
            val listType = object : TypeToken<List<CustomListEntity>>() {}.type
            val lists: List<CustomListEntity> = gson.fromJson(listsArray, listType) ?: emptyList()
            if (lists.isNotEmpty()) {
                customListDao.insertAllLists(lists)
            }
        }

        if (itemsArray != null && !itemsArray.isJsonNull && itemsArray.size() > 0) {
            val itemType = object : TypeToken<List<CustomListItemEntity>>() {}.type
            val items: List<CustomListItemEntity> =
                gson.fromJson(itemsArray, itemType) ?: emptyList()
            if (items.isNotEmpty()) {
                customListDao.insertAllListItems(items)
            }
        }
    }

    override suspend fun getEntityCount(): Int {
        return customListDao.getAllLists().size + customListDao.getAllListItems().size
    }

    override suspend fun getDetailedCounts(): Map<String, Int> {
        return mapOf(
            BackupMetadata.KEY_CUSTOM_LISTS to customListDao.getAllLists().size,
            BackupMetadata.KEY_CUSTOM_LIST_ITEMS to customListDao.getAllListItems().size
        )
    }
}
