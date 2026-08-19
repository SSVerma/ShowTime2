package com.ssverma.shared.data.local.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "custom_list_items",
    primaryKeys = ["listId", "mediaId"],
    foreignKeys = [
        ForeignKey(
            entity = CustomListEntity::class,
            parentColumns = ["listId"],
            childColumns = ["listId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["listId"]),
        Index(value = ["mediaId"])
    ]
)
data class CustomListItemEntity(
    val listId: String,
    val mediaId: Int,
    val mediaType: String,
    val title: String,
    val posterImageUrl: String,
    val backdropImageUrl: String = "",
    val voteAvg: Float = 0f,
    val userNotes: String? = null,
    val rankOrder: Int = 0,
    val addedAt: Long = System.currentTimeMillis()
)
