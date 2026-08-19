package com.ssverma.shared.data.local.db.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_lists")
data class CustomListEntity(
    @PrimaryKey
    val listId: String,
    val title: String,
    val description: String? = null,
    val coverImageUrl: String? = null,
    val isPublic: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
