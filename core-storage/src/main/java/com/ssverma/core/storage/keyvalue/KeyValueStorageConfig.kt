package com.ssverma.core.storage.keyvalue

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

data class KeyValueStorageConfig(
    val fileName: String,
    val corruptionHandler: ReplaceFileCorruptionHandler<Preferences>? = null,
    val produceMigrations: (Context) -> List<DataMigration<Preferences>> = { emptyList() },
    val scope: CoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
)
