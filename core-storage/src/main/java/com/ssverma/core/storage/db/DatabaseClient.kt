package com.ssverma.core.storage.db

import android.content.Context
import androidx.room.RoomDatabase

interface DatabaseClient {
    fun <T : RoomDatabase> createDatabase(
        context: Context,
        config: DatabaseConfig<T>
    ): T
}