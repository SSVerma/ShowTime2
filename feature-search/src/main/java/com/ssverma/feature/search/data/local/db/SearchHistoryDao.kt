package com.ssverma.feature.search.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {
    @Query(
        "SELECT * FROM ${SearchContract.History.TableName} " +
                "ORDER BY ${SearchContract.History.ColTimestamp} DESC " +
                "LIMIT :limit"
    )
    fun loadLatest(limit: Int): Flow<List<LocalSearchHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(history: LocalSearchHistory)

    @Query(
        "DELETE FROM ${SearchContract.History.TableName} " +
                "WHERE ${SearchContract.History.ColId} = :id"
    )
    suspend fun deleteById(id: Int)
}