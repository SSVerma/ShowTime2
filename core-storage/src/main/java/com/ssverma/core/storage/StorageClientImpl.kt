package com.ssverma.core.storage

import android.content.Context
import androidx.room.RoomDatabase
import com.ssverma.core.storage.db.DatabaseClient
import com.ssverma.core.storage.db.DatabaseConfig
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class StorageClientImpl @Inject constructor(
    private val databaseClient: DatabaseClient,
    private val keyValueStorageClient: KeyValueStorageClient
) : StorageClient {

    override fun <T : RoomDatabase> createDatabase(context: Context, config: DatabaseConfig<T>): T {
        return databaseClient.createDatabase(context, config)
    }

    override fun createKeyValueStorage(
        context: Context,
        config: KeyValueStorageConfig
    ): KeyValueStorage {
        return keyValueStorageClient.createKeyValueStorage(context, config)
    }
}