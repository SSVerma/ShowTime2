package com.ssverma.shared.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ssverma.shared.data.local.db.entity.AiringReminderEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AiringReminderDao {
    @Query("SELECT * FROM airing_reminders WHERE isActive = 1 ORDER BY reminderTimeMillis ASC")
    fun getAllActiveFlow(): Flow<List<AiringReminderEntity>>

    @Query("SELECT * FROM airing_reminders WHERE mediaId = :mediaId AND mediaType = :mediaType LIMIT 1")
    fun getByMediaIdFlow(mediaId: Int, mediaType: String): Flow<AiringReminderEntity?>

    @Query("SELECT * FROM airing_reminders WHERE mediaId = :mediaId AND mediaType = :mediaType LIMIT 1")
    suspend fun getByMediaId(mediaId: Int, mediaType: String): AiringReminderEntity?

    @Query("SELECT * FROM airing_reminders WHERE isActive = 1")
    suspend fun getAllActive(): List<AiringReminderEntity>

    @Query("SELECT COUNT(*) FROM airing_reminders WHERE isActive = 1")
    suspend fun getActiveCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: AiringReminderEntity): Long

    @Query("DELETE FROM airing_reminders WHERE mediaId = :mediaId AND mediaType = :mediaType")
    suspend fun deleteByMediaId(mediaId: Int, mediaType: String)

    @Query("DELETE FROM airing_reminders WHERE reminderTimeMillis < :currentTimeMillis")
    suspend fun deleteExpired(currentTimeMillis: Long)

    @Query("DELETE FROM airing_reminders")
    suspend fun clearAll()
}
