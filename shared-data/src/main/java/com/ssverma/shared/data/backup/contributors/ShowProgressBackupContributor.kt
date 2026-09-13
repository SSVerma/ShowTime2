package com.ssverma.shared.data.backup.contributors

import com.google.gson.Gson
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

        if (historyArray != null && !historyArray.isJsonNull && historyArray.size() > 0) {
            val historyType = object : TypeToken<List<EpisodeWatchHistoryEntity>>() {}.type
            val items: List<EpisodeWatchHistoryEntity> =
                gson.fromJson(historyArray, historyType) ?: emptyList()
            if (items.isNotEmpty()) {
                episodeWatchHistoryDao.insertAll(items)
            }
        }

        if (progressArray != null && !progressArray.isJsonNull && progressArray.size() > 0) {
            val progressType = object : TypeToken<List<ShowWatchProgressEntity>>() {}.type
            val cloudItems: List<ShowWatchProgressEntity> =
                gson.fromJson(progressArray, progressType) ?: emptyList()

            for (cloudItem in cloudItems) {
                val localItem = showWatchProgressDao.getProgress(cloudItem.showId)
                if (localItem == null) {
                    showWatchProgressDao.insertOrUpdate(cloudItem)
                } else {
                    // Smart non-destructive merge: progress should never regress backward
                    val localIsFurther = when {
                        localItem.totalCompleted > cloudItem.totalCompleted -> true
                        cloudItem.totalCompleted > localItem.totalCompleted -> false
                        localItem.seasonNumber > cloudItem.seasonNumber -> true
                        cloudItem.seasonNumber > localItem.seasonNumber -> false
                        localItem.episodeNumber > cloudItem.episodeNumber -> true
                        cloudItem.episodeNumber > localItem.episodeNumber -> false
                        else -> localItem.lastWatchedAt >= cloudItem.lastWatchedAt
                    }

                    val base = if (localIsFurther) localItem else cloudItem
                    val watchedCountInDb = episodeWatchHistoryDao.getWatchedCount(cloudItem.showId)
                    val effectiveCompleted = maxOf(
                        base.totalCompleted,
                        maxOf(localItem.totalCompleted, cloudItem.totalCompleted),
                        watchedCountInDb
                    )

                    val merged = base.copy(
                        totalCompleted = effectiveCompleted,
                        seasonCompleted = maxOf(
                            localItem.seasonCompleted,
                            cloudItem.seasonCompleted
                        ),
                        totalAired = maxOf(localItem.totalAired, cloudItem.totalAired),
                        seasonTotalAired = maxOf(
                            localItem.seasonTotalAired,
                            cloudItem.seasonTotalAired
                        ),
                        lastWatchedAt = maxOf(localItem.lastWatchedAt, cloudItem.lastWatchedAt)
                    )
                    showWatchProgressDao.insertOrUpdate(merged)
                }
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
