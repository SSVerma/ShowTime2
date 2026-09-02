package com.ssverma.shared.data.di

import android.content.Context
import com.ssverma.core.storage.keyvalue.KeyValueStorage
import com.ssverma.core.storage.keyvalue.KeyValueStorageClient
import com.ssverma.core.storage.keyvalue.KeyValueStorageConfig
import com.ssverma.shared.data.repository.AffiliateRepositoryImpl
import com.ssverma.shared.data.repository.BackupRepository
import com.ssverma.shared.data.repository.BackupRepositoryImpl
import com.ssverma.shared.data.repository.DefaultAppConfigRepository
import com.ssverma.shared.data.repository.DefaultDiaryRepository
import com.ssverma.shared.data.repository.LibraryRepositoryImpl
import com.ssverma.shared.domain.repository.AffiliateRepository
import com.ssverma.shared.domain.repository.AppConfigRepository
import com.ssverma.shared.domain.repository.DiaryRepository
import com.ssverma.shared.domain.repository.LibraryRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppConfigBindingModule {
    @Singleton
    @Binds
    abstract fun bindAppConfigRepository(
        repository: DefaultAppConfigRepository
    ): AppConfigRepository

    @Singleton
    @Binds
    abstract fun bindAffiliateRepository(
        repository: AffiliateRepositoryImpl
    ): AffiliateRepository

    @Singleton
    @Binds
    abstract fun bindLibraryRepository(
        repository: LibraryRepositoryImpl
    ): LibraryRepository

    @Singleton
    @Binds
    abstract fun bindBackupRepository(
        repository: BackupRepositoryImpl
    ): BackupRepository

    @Singleton
    @Binds
    abstract fun bindDiaryRepository(
        repository: DefaultDiaryRepository
    ): DiaryRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AppConfigModule {

    @Singleton
    @Provides
    @Named("app_config")
    fun provideAppConfigKeyValueStorage(
        @ApplicationContext
        context: Context,
        keyValueStorageClient: KeyValueStorageClient
    ): KeyValueStorage {
        return keyValueStorageClient.createKeyValueStorage(
            context = context,
            config = KeyValueStorageConfig(fileName = "app_config")
        )
    }
}
