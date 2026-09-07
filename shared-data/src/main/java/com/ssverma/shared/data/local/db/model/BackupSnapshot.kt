package com.ssverma.shared.data.local.db.model

import com.ssverma.shared.data.local.db.entity.CustomListEntity
import com.ssverma.shared.data.local.db.entity.CustomListItemEntity
import com.ssverma.shared.data.local.db.entity.DiaryEntryEntity
import com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.FavoriteEntity
import com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity
import com.ssverma.shared.data.local.db.entity.WatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.WatchlistEntity
import com.ssverma.shared.domain.model.challenge.BlindspotPriorityItem
import com.ssverma.shared.domain.model.challenge.CinephileChallenge
import com.ssverma.shared.domain.model.game.CinemaGameStats

data class BackupSnapshot(
    val version: Int = 2,
    val timestamp: Long = System.currentTimeMillis(),
    val deviceName: String = "",
    val favorites: List<FavoriteEntity> = emptyList(),
    val watchlist: List<WatchlistEntity> = emptyList(),
    val history: List<WatchHistoryEntity> = emptyList(),
    val customLists: List<CustomListEntity> = emptyList(),
    val customListItems: List<CustomListItemEntity> = emptyList(),
    val diaryEntries: List<DiaryEntryEntity> = emptyList(),
    val showProgress: List<ShowWatchProgressEntity> = emptyList(),
    val episodeHistory: List<EpisodeWatchHistoryEntity> = emptyList(),
    val activeChallenges: List<CinephileChallenge> = emptyList(),
    val blindspots: List<BlindspotPriorityItem> = emptyList(),
    val gameStats: CinemaGameStats? = null,
    val preferences: Map<String, String> = emptyMap()
)
