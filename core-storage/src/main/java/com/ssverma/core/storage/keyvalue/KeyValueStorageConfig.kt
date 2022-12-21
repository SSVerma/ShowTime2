package com.ssverma.core.storage.keyvalue

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences

data class KeyValueStorageConfig(
    val fileName: String,
    val corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    val produceMigrations: (Context) -> List<DataMigration<Preferences>> = { emptyList() }
)
