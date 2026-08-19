package com.ssverma.shared.data.local.db.model

import com.ssverma.shared.data.local.db.entity.CustomListEntity
import com.ssverma.shared.data.local.db.entity.CustomListItemEntity
import com.ssverma.shared.data.local.db.entity.FavoriteEntity
import com.ssverma.shared.data.local.db.entity.WatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.WatchlistEntity

data class BackupSnapshot(
    val version: Int = 2,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceName: String = "",
    val favorites: List<FavoriteEntity> = emptyList(),
    val watchlist: List<WatchlistEntity> = emptyList(),
    val history: List<WatchHistoryEntity> = emptyList(),
    val customLists: List<CustomListEntity> = emptyList(),
    val customListItems: List<CustomListItemEntity> = emptyList(),
    val preferences: Map<String, String> = emptyMap()
)
