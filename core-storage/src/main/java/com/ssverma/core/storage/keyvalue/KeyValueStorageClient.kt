package com.ssverma.core.storage.keyvalue

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences

typealias KeyValueStorage = DataStore<Preferences>

interface KeyValueStorageClient {
    fun createKeyValueStorage(
        context: Context,
        config: KeyValueStorageConfig
    ): KeyValueStorage
}