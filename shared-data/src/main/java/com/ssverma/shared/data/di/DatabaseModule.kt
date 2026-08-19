package com.ssverma.shared.data.di

import android.content.Context
import com.ssverma.core.storage.StorageClient
import com.ssverma.core.storage.db.DatabaseConfig
import com.ssverma.shared.data.local.db.ShowTimeDatabase
import com.ssverma.shared.data.local.db.dao.CustomListDao
import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideShowTimeDatabase(
        @ApplicationContext context: Context,
        storageClient: StorageClient
    ): ShowTimeDatabase {
        return storageClient.createDatabase(
            context = context,
            config = DatabaseConfig(
                databaseName = "showtime_database",
                databaseClass = ShowTimeDatabase::class.java
            )
        )
    }

    @Provides
    @Singleton
    fun provideFavoriteDao(database: ShowTimeDatabase): FavoriteDao {
        return database.favoriteDao()
    }

    @Provides
    @Singleton
    fun provideWatchlistDao(database: ShowTimeDatabase): WatchlistDao {
        return database.watchlistDao()
    }

    @Provides
    @Singleton
    fun provideWatchHistoryDao(database: ShowTimeDatabase): WatchHistoryDao {
        return database.watchHistoryDao()
    }

    @Provides
    @Singleton
    fun provideCustomListDao(database: ShowTimeDatabase): CustomListDao {
        return database.customListDao()
    }
}
