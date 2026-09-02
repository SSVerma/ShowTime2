package com.ssverma.shared.domain.usecase.diary

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.diary.DiaryFilterType
import com.ssverma.shared.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetDiaryEntriesUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(
        filterType: DiaryFilterType = DiaryFilterType.ALL
    ): Flow<List<DiaryEntry>> {
        return diaryRepository.getAllDiaryEntries().map { entries ->
            when (filterType) {
                DiaryFilterType.ALL -> entries
                DiaryFilterType.MOVIES_ONLY -> entries.filter { it.mediaType == MediaType.Movie }
                DiaryFilterType.TV_ONLY -> entries.filter { it.mediaType == MediaType.Tv }
                DiaryFilterType.REWATCHES_ONLY -> entries.filter { it.isRewatch }
                DiaryFilterType.FIVE_STARS_ONLY -> entries.filter { it.userRating >= 4.5f }
            }
        }
    }

    fun forMedia(mediaId: Int, mediaType: MediaType): Flow<List<DiaryEntry>> {
        return diaryRepository.getDiaryEntriesForMedia(mediaId, mediaType)
    }
}
