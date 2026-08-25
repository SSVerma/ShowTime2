package com.ssverma.shared.data.local.db.entity

import androidx.room.Entity

@Entity(
    tableName = "episode_watch_history",
    primaryKeys = ["showId", "seasonNumber", "episodeNumber"]
)
data class EpisodeWatchHistoryEntity(
    val showId: Int,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val watchedAt: Long = System.currentTimeMillis()
)
