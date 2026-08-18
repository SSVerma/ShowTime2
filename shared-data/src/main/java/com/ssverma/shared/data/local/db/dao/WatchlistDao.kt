package com.ssverma.shared.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ssverma.shared.data.local.db.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun getAllWatchlistFlow(): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist WHERE mediaType = :mediaType ORDER BY addedAt DESC")
    fun getWatchlistByTypeFlow(mediaType: String): Flow<List<WatchlistEntity>>

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    suspend fun getAllWatchlist(): List<WatchlistEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE mediaId = :mediaId)")
    fun isInWatchlistFlow(mediaId: Int): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE mediaId = :mediaId)")
    suspend fun isInWatchlist(mediaId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchlist(item: WatchlistEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<WatchlistEntity>)

    @Query("DELETE FROM watchlist WHERE mediaId = :mediaId")
    suspend fun deleteWatchlistById(mediaId: Int)

    @Query("DELETE FROM watchlist")
    suspend fun clearWatchlist()
}
