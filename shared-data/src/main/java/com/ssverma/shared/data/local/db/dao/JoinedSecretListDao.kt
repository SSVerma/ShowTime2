package com.ssverma.shared.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ssverma.shared.data.local.db.entity.JoinedSecretListEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface JoinedSecretListDao {

    @Query("SELECT * FROM joined_secret_lists ORDER BY lastOpenedEpochMs DESC")
    fun getAllJoinedListsFlow(): Flow<List<JoinedSecretListEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(list: JoinedSecretListEntity)

    @Query("SELECT * FROM joined_secret_lists")
    suspend fun getAllJoinedLists(): List<JoinedSecretListEntity>

    @Query("DELETE FROM joined_secret_lists WHERE shareCode = :shareCode")
    suspend fun deleteByShareCode(shareCode: String)
}
