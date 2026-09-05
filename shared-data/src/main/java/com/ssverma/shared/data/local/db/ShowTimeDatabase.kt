package com.ssverma.shared.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ssverma.shared.data.local.db.dao.CustomListDao
import com.ssverma.shared.data.local.db.dao.DiaryDao
import com.ssverma.shared.data.local.db.dao.EpisodeWatchHistoryDao
import com.ssverma.shared.data.local.db.dao.FavoriteDao
import com.ssverma.shared.data.local.db.dao.ShowWatchProgressDao
import com.ssverma.shared.data.local.db.dao.WatchHistoryDao
import com.ssverma.shared.data.local.db.dao.WatchlistDao
import com.ssverma.shared.data.local.db.entity.CustomListEntity
import com.ssverma.shared.data.local.db.entity.CustomListItemEntity
import com.ssverma.shared.data.local.db.entity.DiaryEntryEntity
import com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.FavoriteEntity
import com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity
import com.ssverma.shared.data.local.db.entity.WatchHistoryEntity
import com.ssverma.shared.data.local.db.entity.WatchlistEntity

@Database(
    entities = [
        FavoriteEntity::class,
        WatchlistEntity::class,
        WatchHistoryEntity::class,
        CustomListEntity::class,
        CustomListItemEntity::class,
        EpisodeWatchHistoryEntity::class,
        ShowWatchProgressEntity::class,
        DiaryEntryEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class ShowTimeDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun watchlistDao(): WatchlistDao
    abstract fun watchHistoryDao(): WatchHistoryDao
    abstract fun customListDao(): CustomListDao
    abstract fun episodeWatchHistoryDao(): EpisodeWatchHistoryDao
    abstract fun showWatchProgressDao(): ShowWatchProgressDao
    abstract fun diaryDao(): DiaryDao
}
