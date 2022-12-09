package com.ssverma.core.storage.typedobject

import android.content.Context
import androidx.datastore.core.DataStoreFactory
import androidx.datastore.dataStoreFile
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
internal class ObjectStorageClientImpl @Inject constructor() : ObjectStorageClient {
    override fun <T> createObjectStorage(
        context: Context,
        config: ObjectStorageConfig<T>
    ): ObjectStorage<T> {
        return DataStoreFactory.create(
            serializer = config.serializer,
            corruptionHandler = config.corruptionHandler,
            migrations = config.produceMigrations(context.applicationContext),
            scope = CoroutineScope(Dispatchers.IO + SupervisorJob()),
            produceFile = { context.applicationContext.dataStoreFile(config.fileName) }
        )
    }
}