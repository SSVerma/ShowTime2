package com.ssverma.core.storage.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class DatabaseClientImpl @Inject constructor() : DatabaseClient {
    override fun <T : RoomDatabase> createDatabase(
        context: Context,
        config: DatabaseConfig<T>
    ): T {
        return Room.databaseBuilder(
            context.applicationContext,
            config.databaseClass,
            config.databaseName
        ).fallbackToDestructiveMigration()
            .build()
    }
}