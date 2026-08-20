package com.ssverma.core.backup.model

data class BackupMetadata(
    val timestamp: Long,
    val formattedDate: String,
    val sizeBytes: Long,
    val formattedSize: String,
    val deviceName: String,
    val favoritesCount: Int,
    val watchlistCount: Int,
    val historyCount: Int,
    val customListsCount: Int = 0,
    val customListItemsCount: Int = 0
)
