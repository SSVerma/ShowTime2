package com.ssverma.core.storage.keyvalue

import android.content.Context
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class KeyValueStorageClientImpl @Inject constructor() : KeyValueStorageClient {
    override fun createKeyValueStorage(
        context: Context,
        config: KeyValueStorageConfig
    ): KeyValueStorage {
        return PreferenceDataStoreFactory.create(
            corruptionHandler = config.corruptionHandler,
            migrations = config.produceMigrations(context.applicationContext),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
        ) {
            context.applicationContext.preferencesDataStoreFile(config.fileName)
        }
    }
}