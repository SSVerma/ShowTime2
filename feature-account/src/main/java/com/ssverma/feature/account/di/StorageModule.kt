package com.ssverma.feature.account.di

import android.content.Context
import com.ssverma.core.storage.typedobject.ObjectStorage
import com.ssverma.core.storage.typedobject.ObjectStorageClient
import com.ssverma.core.storage.typedobject.ObjectStorageConfig
import com.ssverma.feature.account.data.local.serializer.UserAccountSerializer
import com.ssverma.showtime.UserAccount
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

private const val UserAccountFileName = "user_account.proto"

@Module
@InstallIn(SingletonComponent::class)
object StorageModule {

    @Singleton
    @Provides
    fun provideObjectStorage(
        @ApplicationContext
        context: Context,
        objectStorageClient: ObjectStorageClient
    ): ObjectStorage<UserAccount> {
        return objectStorageClient.createObjectStorage(
            context = context,
            config = ObjectStorageConfig(
                fileName = UserAccountFileName,
                serializer = UserAccountSerializer
            )
        )
    }
}