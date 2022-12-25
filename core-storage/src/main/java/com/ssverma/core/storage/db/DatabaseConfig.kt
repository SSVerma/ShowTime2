package com.ssverma.core.storage.db

import androidx.room.RoomDatabase

data class DatabaseConfig<T : RoomDatabase>(
    val databaseName: String,
    val databaseClass: Class<T>
)