package com.ssverma.shared.data.local.db.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "diary_entries",
    indices = [
        Index(value = ["loggedAt"]),
        Index(value = ["mediaId", "mediaType"])
    ]
)
data class DiaryEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val mediaId: Int,
    val mediaType: String,
    val title: String,
    val posterImageUrl: String,
    val backdropImageUrl: String = "",
    val releaseDate: String = "",
    val tmdbRating: Float = 0f,
    val userRating: Float,
    val review: String = "",
    val isRewatch: Boolean = false,
    val loggedAt: Long = System.currentTimeMillis(),
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null
)
