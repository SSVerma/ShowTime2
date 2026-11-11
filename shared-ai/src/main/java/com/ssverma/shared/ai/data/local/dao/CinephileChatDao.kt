package com.ssverma.shared.ai.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.ssverma.shared.ai.data.local.entity.CinephileMessageEntity
import com.ssverma.shared.ai.data.local.entity.CinephileThreadEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CinephileChatDao {
    @Query("SELECT * FROM cinephile_chat_threads ORDER BY updatedAt DESC")
    fun getThreads(): Flow<List<CinephileThreadEntity>>

    @Query("SELECT * FROM cinephile_chat_threads WHERE id = :threadId LIMIT 1")
    suspend fun getThreadById(threadId: String): CinephileThreadEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertThread(thread: CinephileThreadEntity)

    @Update
    suspend fun updateThread(thread: CinephileThreadEntity)

    @Query("DELETE FROM cinephile_chat_threads WHERE id = :threadId")
    suspend fun deleteThread(threadId: String)

    @Query("SELECT * FROM cinephile_chat_messages WHERE threadId = :threadId ORDER BY timestamp ASC")
    fun getMessagesForThread(threadId: String): Flow<List<CinephileMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: CinephileMessageEntity)

    @Query("DELETE FROM cinephile_chat_messages WHERE threadId = :threadId")
    suspend fun deleteMessagesForThread(threadId: String)

    @Query("DELETE FROM cinephile_chat_threads")
    suspend fun clearAllThreads()

    @Query("DELETE FROM cinephile_chat_messages")
    suspend fun clearAllMessages()
}
