package com.ssverma.shared.ai.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "cinephile_chat_messages",
    indices = [
        Index(value = ["threadId"]),
    ]
)
data class CinephileMessageEntity(
    @PrimaryKey
    val id: String,
    val threadId: String,
    val role: String,
    val text: String,
    val a2uiJson: String?,
    val timestamp: Long,
)
