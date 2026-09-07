package com.ssverma.shared.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "airing_reminders")
data class AiringReminderEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val mediaId: Int,
    val mediaType: String,
    val reminderType: String,
    val mediaTitle: String,
    val posterImageUrl: String,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeTitle: String? = null,
    val airDate: String,
    val reminderTimeMillis: Long,
    val providerName: String? = null,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
