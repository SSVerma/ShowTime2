package com.ssverma.core.storage.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ssverma.core.storage.db.DatabaseConfig

/**
 * We can create singleton inside the core-storage module itself by providing one lookup table.
 * Currently singleton management is left on the consumer modules.
 *
 * */
internal object DatabaseProvider {
    private val dbEntries = mutableMapOf<String, RoomDatabase>()

    @Synchronized
    fun <T : RoomDatabase> provideRoomDb(
        applicationContext: Context,
        dbConfig: DatabaseConfig<T>
    ): T {
        if (dbEntries[dbConfig.databaseName] == null) {
            dbEntries[dbConfig.databaseName] = Room.databaseBuilder(
                applicationContext,
                dbConfig.databaseClass,
                dbConfig.databaseName
            ).fallbackToDestructiveMigration()
                .build()
        }
        return (dbEntries[dbConfig.databaseName] as T?)!!
    }
}