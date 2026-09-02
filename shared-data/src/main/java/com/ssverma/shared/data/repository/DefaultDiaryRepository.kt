package com.ssverma.shared.data.repository

import com.ssverma.shared.data.local.db.dao.DiaryDao
import com.ssverma.shared.data.local.db.entity.DiaryEntryEntity
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.diary.DiarySummaryStats
import com.ssverma.shared.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultDiaryRepository @Inject constructor(
    private val diaryDao: DiaryDao
) : DiaryRepository {

    override fun getAllDiaryEntries(): Flow<List<DiaryEntry>> {
        return diaryDao.getAllDiaryEntries().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getDiaryEntriesForMedia(
        mediaId: Int,
        mediaType: MediaType
    ): Flow<List<DiaryEntry>> {
        val mediaTypeString = if (mediaType == MediaType.Tv) "tv" else "movie"
        return diaryDao.getDiaryEntriesForMedia(
            mediaId = mediaId,
            mediaType = mediaTypeString
        ).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getDiaryEntryById(id: Long): DiaryEntry? {
        return diaryDao.getDiaryEntryById(id)?.toDomain()
    }

    override suspend fun saveDiaryEntry(entry: DiaryEntry): Long {
        return diaryDao.insertDiaryEntry(entry.toEntity())
    }

    override suspend fun deleteDiaryEntry(id: Long) {
        diaryDao.deleteDiaryEntryById(id)
    }

    override fun getDiarySummaryStats(): Flow<DiarySummaryStats> {
        return diaryDao.getAllDiaryEntries().map { entries ->
            if (entries.isEmpty()) {
                DiarySummaryStats()
            } else {
                val movies = entries.count { it.mediaType.equals("movie", ignoreCase = true) }
                val tvShows = entries.count { it.mediaType.equals("tv", ignoreCase = true) }
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

    private fun DiaryEntryEntity.toDomain(): DiaryEntry {
        return DiaryEntry(
            id = id,
            mediaId = mediaId,
            mediaType = if (mediaType.equals(
                    "tv",
                    ignoreCase = true
                )
            ) MediaType.Tv else MediaType.Movie,
            title = title,
            posterImageUrl = posterImageUrl,
            backdropImageUrl = backdropImageUrl,
            releaseDate = releaseDate,
            tmdbRating = tmdbRating,
            userRating = userRating,
            review = review,
            isRewatch = isRewatch,
            loggedAt = loggedAt,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber
        )
    }

    private fun DiaryEntry.toEntity(): DiaryEntryEntity {
        return DiaryEntryEntity(
            id = id,
            mediaId = mediaId,
            mediaType = if (mediaType == MediaType.Tv) "tv" else "movie",
            title = title,
            posterImageUrl = posterImageUrl,
            backdropImageUrl = backdropImageUrl,
            releaseDate = releaseDate,
            tmdbRating = tmdbRating,
            userRating = userRating,
            review = review,
            isRewatch = isRewatch,
            loggedAt = loggedAt,
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber
        )
    }
}
