package com.ssverma.feature.search.di

import android.content.Context
import com.ssverma.core.storage.db.DatabaseClient
import com.ssverma.core.storage.db.DatabaseConfig
import com.ssverma.feature.search.data.local.db.SearchDatabase
import com.ssverma.feature.search.data.local.db.SearchHistoryDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object SearchDbModule {

    @Singleton
    @Provides
    fun provideSearchDb(
        @ApplicationContext context: Context,
        databaseClient: DatabaseClient
    ): SearchDatabase {
        return databaseClient.createDatabase(
            context = context,
            config = DatabaseConfig(
                databaseName = "search",
                databaseClass = SearchDatabase::class.java
            )
        )
    }

    @Singleton
    @Provides
    fun provideHistoryDao(searchDatabase: SearchDatabase): SearchHistoryDao {
        return searchDatabase.historyDao()
    }
}