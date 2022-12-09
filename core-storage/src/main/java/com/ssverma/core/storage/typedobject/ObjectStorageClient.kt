package com.ssverma.core.storage.typedobject

import android.content.Context
import androidx.datastore.core.DataStore

typealias ObjectStorage<T> = DataStore<T>

interface ObjectStorageClient {
    fun <T> createObjectStorage(
        context: Context,
        config: ObjectStorageConfig<T>
    ): ObjectStorage<T>
}
