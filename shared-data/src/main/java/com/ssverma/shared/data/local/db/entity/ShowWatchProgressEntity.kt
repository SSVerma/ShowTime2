package com.ssverma.shared.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "show_watch_progress")
data class ShowWatchProgressEntity(
    @PrimaryKey
    val showId: Int,
    val showTitle: String,
    val showPosterPath: String?,
    val seasonNumber: Int,
    val episodeNumber: Int,
    val episodeTitle: String?,
    val seasonCompleted: Int = 0,
    val seasonTotalAired: Int = 0,
    val totalCompleted: Int,
    val totalAired: Int,
    val lastWatchedAt: Long = System.currentTimeMillis()
)
