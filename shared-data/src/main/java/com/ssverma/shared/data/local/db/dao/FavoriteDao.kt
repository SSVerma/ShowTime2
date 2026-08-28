package com.ssverma.shared.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ssverma.shared.data.local.db.entity.FavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun getAllFavoritesFlow(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites WHERE mediaType = :mediaType ORDER BY addedAt DESC")
    fun getFavoritesByTypeFlow(mediaType: String): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    suspend fun getAllFavorites(): List<FavoriteEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mediaId = :mediaId)")
    fun isFavoriteFlow(mediaId: Int): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE mediaId = :mediaId)")
    suspend fun isFavorite(mediaId: Int): Boolean

    @Query(
        """
        SELECT EXISTS (
            SELECT 1 FROM favorites WHERE mediaId = :mediaId
            UNION ALL
            SELECT 1 FROM watchlist WHERE mediaId = :mediaId
            UNION ALL
            SELECT 1 FROM watch_history WHERE mediaId = :mediaId
            UNION ALL
            SELECT 1 FROM custom_list_items WHERE mediaId = :mediaId
        )
    """
    )
    fun isMediaActionActiveFlow(mediaId: Int): Flow<Boolean>

    @Query(
        """
        SELECT EXISTS (
            SELECT 1 FROM favorites WHERE mediaId = :mediaId
            UNION ALL
            SELECT 1 FROM watchlist WHERE mediaId = :mediaId
            UNION ALL
            SELECT 1 FROM watch_history WHERE mediaId = :mediaId
            UNION ALL
            SELECT 1 FROM custom_list_items WHERE mediaId = :mediaId
        )
    """
    )
    suspend fun isMediaActionActive(mediaId: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(favorites: List<FavoriteEntity>)

    @Query("DELETE FROM favorites WHERE mediaId = :mediaId")
    suspend fun deleteFavoriteById(mediaId: Int)

    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()
}
