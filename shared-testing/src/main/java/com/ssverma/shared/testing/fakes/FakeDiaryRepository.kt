package com.ssverma.shared.testing.fakes

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.diary.DiarySummaryStats
import com.ssverma.shared.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

class FakeDiaryRepository : DiaryRepository {

    private val entriesFlow = MutableStateFlow<List<DiaryEntry>>(emptyList())

    override fun getAllDiaryEntries(): Flow<List<DiaryEntry>> = entriesFlow

    override fun getDiaryEntriesForMedia(
        mediaId: Int,
        mediaType: MediaType
    ): Flow<List<DiaryEntry>> {
        return entriesFlow.map { list ->
            list.filter { it.mediaId == mediaId && it.mediaType == mediaType }
        }
    }

    override suspend fun getDiaryEntryForMedia(
        mediaId: Int,
        mediaType: MediaType
    ): DiaryEntry? {
        return entriesFlow.value.find { it.mediaId == mediaId && it.mediaType == mediaType }
    }

    override suspend fun getDiaryEntryById(id: Long): DiaryEntry? {
        return entriesFlow.value.find { it.id == id }
    }

    override suspend fun saveDiaryEntry(entry: DiaryEntry): Long {
        val current = entriesFlow.value.toMutableList()
        val existing = if (entry.id == 0L) {
            current.find { it.mediaId == entry.mediaId && it.mediaType == entry.mediaType }
        } else {
            current.find { it.id == entry.id }
        }

        val finalId = existing?.id ?: if (entry.id == 0L) (current.maxOfOrNull { it.id }
            ?: 0L) + 1L else entry.id
        val finalEntry = entry.copy(
            id = finalId,
            loggedAt = if (entry.loggedAt != 0L) entry.loggedAt else existing?.loggedAt
                ?: System.currentTimeMillis()
        )
        val index = current.indexOfFirst { it.id == finalId }
        if (index >= 0) {
            current[index] = finalEntry
        } else {
            current.add(0, finalEntry)
        }
        entriesFlow.value = current
        return finalId
    }

    override suspend fun deleteDiaryEntry(id: Long) {
        val current = entriesFlow.value.toMutableList()
        current.removeAll { it.id == id }
        entriesFlow.value = current
    }

    override fun getDiarySummaryStats(): Flow<DiarySummaryStats> {
        return entriesFlow.map { entries ->
            if (entries.isEmpty()) {
                DiarySummaryStats()
            } else {
                val movies = entries.count { it.mediaType == MediaType.Movie }
                val tvShows = entries.count { it.mediaType == MediaType.Tv }
                val avgRating = entries.map { it.userRating }.average().toFloat()
                val rewatches = entries.count { it.isRewatch }
                val fiveStars = entries.count { it.userRating >= 4.5f }

                DiarySummaryStats(
                    totalLogged = entries.size,
                    totalMovies = movies,
                    totalTvShows = tvShows,
                    averageUserRating = if (avgRating.isNaN()) 0f else avgRating,
                    rewatchCount = rewatches,
                    fiveStarCount = fiveStars
                )
            }
        }
    }
}
