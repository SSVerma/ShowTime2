package com.ssverma.shared.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "joined_secret_lists")
data class JoinedSecretListEntity(
    @PrimaryKey
    val shareCode: String,
    val title: String,
    val description: String? = null,
    val ownerName: String,
    val coverImageUrl: String? = null,
    val itemCount: Int = 0,
    val isCollaborative: Boolean = false,
    val lastOpenedEpochMs: Long = System.currentTimeMillis()
)
