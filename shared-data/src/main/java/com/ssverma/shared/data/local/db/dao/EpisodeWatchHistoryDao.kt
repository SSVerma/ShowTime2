package com.ssverma.shared.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ssverma.shared.data.local.db.entity.EpisodeWatchHistoryEntity
import kotlinx.coroutines.flow.Flow

data class SeasonWatchCount(
    val seasonNumber: Int,
    val watchedCount: Int
)

@Dao
interface EpisodeWatchHistoryDao {
    @Query("SELECT episodeNumber FROM episode_watch_history WHERE showId = :showId AND seasonNumber = :seasonNumber")
    fun getWatchedEpisodeNumbersFlow(showId: Int, seasonNumber: Int): Flow<List<Int>>

    @Query("SELECT episodeNumber FROM episode_watch_history WHERE showId = :showId AND seasonNumber = :seasonNumber")
    suspend fun getWatchedEpisodeNumbers(showId: Int, seasonNumber: Int): List<Int>

    @Query("SELECT DISTINCT seasonNumber FROM episode_watch_history WHERE showId = :showId")
    fun getWatchedSeasonsFlow(showId: Int): Flow<List<Int>>

    @Query("SELECT seasonNumber, COUNT(*) as watchedCount FROM episode_watch_history WHERE showId = :showId GROUP BY seasonNumber")
    fun getSeasonWatchCountsFlow(showId: Int): Flow<List<SeasonWatchCount>>

    @Query("SELECT COUNT(*) FROM episode_watch_history WHERE showId = :showId")
    suspend fun getWatchedCount(showId: Int): Int

    @Query("SELECT * FROM episode_watch_history WHERE showId = :showId")
    suspend fun getAllWatchedEpisodes(showId: Int): List<EpisodeWatchHistoryEntity>

    @Query("SELECT EXISTS(SELECT 1 FROM episode_watch_history WHERE showId = :showId AND seasonNumber = :seasonNumber AND episodeNumber = :episodeNumber)")
    fun isEpisodeWatchedFlow(showId: Int, seasonNumber: Int, episodeNumber: Int): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM episode_watch_history WHERE showId = :showId AND seasonNumber = :seasonNumber AND episodeNumber = :episodeNumber)")
    suspend fun isEpisodeWatched(showId: Int, seasonNumber: Int, episodeNumber: Int): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEpisode(item: EpisodeWatchHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<EpisodeWatchHistoryEntity>)

    @Query("DELETE FROM episode_watch_history WHERE showId = :showId AND seasonNumber = :seasonNumber AND episodeNumber = :episodeNumber")
    suspend fun deleteEpisode(showId: Int, seasonNumber: Int, episodeNumber: Int)

    @Query("DELETE FROM episode_watch_history WHERE showId = :showId AND seasonNumber = :seasonNumber")
    suspend fun deleteSeason(showId: Int, seasonNumber: Int)

    @Query("DELETE FROM episode_watch_history WHERE showId = :showId")
    suspend fun deleteShow(showId: Int)

    @Query("DELETE FROM episode_watch_history")
    suspend fun clearAll()
}
