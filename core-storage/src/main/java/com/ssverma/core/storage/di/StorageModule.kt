package com.ssverma.core.storage.di

import com.ssverma.core.storage.StorageClient
import com.ssverma.core.storage.StorageClientImpl
import com.ssverma.core.storage.db.DatabaseClient
import com.ssverma.core.storage.db.DatabaseClientImpl
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageClientImpl
import com.ssverma.core.storage.typedobject.ObjectStorageClient
import com.ssverma.core.storage.typedobject.ObjectStorageClientImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class StorageModule {

    @Singleton
    @Binds
    internal abstract fun bindDatabaseClient(
        databaseClientImpl: DatabaseClientImpl
    ): DatabaseClient

    @Singleton
    @Binds
    internal abstract fun bindKeyValueStorageClient(
        keyValueStorageClientImpl: KeyValueStorageClientImpl
    ): KeyValueStorageClient

    @Singleton
    @Binds
    internal abstract fun bindObjectStorageClient(
        objectStorageClientImpl: ObjectStorageClientImpl
    ): ObjectStorageClient

    @Singleton
    @Binds
    internal abstract fun bindStorageClient(
        storageClientImpl: StorageClientImpl
    ): StorageClient
}