package com.ssverma.core.storage.typedobject

import android.content.Context
import androidx.datastore.core.DataMigration
import androidx.datastore.core.Serializer
import androidx.datastore.core.handlers.ReplaceFileCorruptionHandler

data class ObjectStorageConfig<T>(
    val fileName: String,
    val serializer: Serializer<T>,
    val corruptionHandler: ReplaceFileCorruptionHandler<T>? = null,
    val produceMigrations: (Context) -> List<DataMigration<T>> = { emptyList() }
)
