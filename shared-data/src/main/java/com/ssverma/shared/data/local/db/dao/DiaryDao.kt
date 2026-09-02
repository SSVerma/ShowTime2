package com.ssverma.shared.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.ssverma.shared.data.local.db.entity.DiaryEntryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DiaryDao {

    @Query("SELECT * FROM diary_entries ORDER BY loggedAt DESC")
    fun getAllDiaryEntries(): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE mediaId = :mediaId AND mediaType = :mediaType ORDER BY loggedAt DESC")
    fun getDiaryEntriesForMedia(mediaId: Int, mediaType: String): Flow<List<DiaryEntryEntity>>

    @Query("SELECT * FROM diary_entries WHERE id = :id LIMIT 1")
    suspend fun getDiaryEntryById(id: Long): DiaryEntryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDiaryEntry(entry: DiaryEntryEntity): Long

    @Query("DELETE FROM diary_entries WHERE id = :id")
    suspend fun deleteDiaryEntryById(id: Long): Int

    @Query("DELETE FROM diary_entries")
    suspend fun deleteAllDiaryEntries()
}
