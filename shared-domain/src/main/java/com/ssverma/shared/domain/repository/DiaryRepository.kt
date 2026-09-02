package com.ssverma.shared.domain.repository

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.diary.DiarySummaryStats
import kotlinx.coroutines.flow.Flow

interface DiaryRepository {
    fun getAllDiaryEntries(): Flow<List<DiaryEntry>>
    fun getDiaryEntriesForMedia(mediaId: Int, mediaType: MediaType): Flow<List<DiaryEntry>>
    suspend fun getDiaryEntryById(id: Long): DiaryEntry?
    suspend fun saveDiaryEntry(entry: DiaryEntry): Long
    suspend fun deleteDiaryEntry(id: Long)
    fun getDiarySummaryStats(): Flow<DiarySummaryStats>
}
