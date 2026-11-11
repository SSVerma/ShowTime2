package com.ssverma.shared.ai.di

import android.content.Context
import com.ssverma.core.storage.db.DatabaseClient
import com.ssverma.core.storage.db.DatabaseConfig
import com.ssverma.shared.ai.data.local.dao.CinephileChatDao
import com.ssverma.shared.ai.data.local.db.CinephileChatDatabase
import com.ssverma.shared.ai.data.repository.CinephileChatRepositoryImpl
import com.ssverma.shared.ai.domain.repository.CinephileChatRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CinephileAiDbModule {

    @Binds
    @Singleton
    abstract fun bindCinephileChatRepository(
        impl: CinephileChatRepositoryImpl,
    ): CinephileChatRepository

    companion object {
        @Singleton
        @Provides
        fun provideCinephileChatDatabase(
            @ApplicationContext context: Context,
            databaseClient: DatabaseClient
        ): CinephileChatDatabase {
            return databaseClient.createDatabase(
                context = context,
                config = DatabaseConfig(
                    databaseName = "cinephile_ai_chat",
                    databaseClass = CinephileChatDatabase::class.java
                )
            )
        }

        @Singleton
        @Provides
        fun provideCinephileChatDao(database: CinephileChatDatabase): CinephileChatDao {
            return database.chatDao()
        }
    }
}
