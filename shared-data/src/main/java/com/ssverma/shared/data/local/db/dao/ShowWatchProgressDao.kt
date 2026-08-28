package com.ssverma.shared.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ssverma.shared.data.local.db.entity.ShowWatchProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ShowWatchProgressDao {
    @Query("SELECT * FROM show_watch_progress WHERE totalAired == 0 OR totalCompleted < totalAired ORDER BY lastWatchedAt DESC")
    fun getUpNextQueueFlow(): Flow<List<ShowWatchProgressEntity>>

    @Query("SELECT * FROM show_watch_progress WHERE totalAired == 0 OR totalCompleted < totalAired ORDER BY lastWatchedAt DESC")
    suspend fun getUpNextQueue(): List<ShowWatchProgressEntity>

    @Query("SELECT * FROM show_watch_progress WHERE showId = :showId")
    suspend fun getProgress(showId: Int): ShowWatchProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: ShowWatchProgressEntity)

    @Query("DELETE FROM show_watch_progress WHERE showId = :showId")
    suspend fun deleteByShowId(showId: Int)

    @Query("DELETE FROM show_watch_progress")
    suspend fun clearAll()
}
