package com.ssverma.feature.search.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [LocalSearchHistory::class],
    version = 1
)
abstract class SearchDatabase : RoomDatabase() {
    abstract fun historyDao(): SearchHistoryDao
}