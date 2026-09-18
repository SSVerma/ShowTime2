package com.ssverma.feature.search.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LocalSearchHistory::class],
    version = 1,
    exportSchema = false
)
abstract class SearchDatabase : RoomDatabase() {
    abstract fun historyDao(): SearchHistoryDao
}