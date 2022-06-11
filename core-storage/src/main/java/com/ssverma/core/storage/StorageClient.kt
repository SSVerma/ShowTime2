package com.ssverma.core.storage

import android.content.Context
import androidx.room.RoomDatabase
import com.ssverma.core.storage.db.DatabaseConfig
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig

interface StorageClient {
    fun <T : RoomDatabase> createDatabase(
        context: Context,
        config: DatabaseConfig<T>
    ): T

    fun createKeyValueStorage(
        context: Context,
        config: KeyValueStorageConfig
    ): KeyValueStorage
}