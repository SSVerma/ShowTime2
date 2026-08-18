package com.ssverma.shared.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "watch_history")
data class WatchHistoryEntity(
    @PrimaryKey
    val mediaId: Int,
    val mediaType: String,
    val title: String,
    val posterImageUrl: String,
    val voteAvg: Float,
    val watchedAt: Long = System.currentTimeMillis()
)
