package com.ssverma.shared.domain.usecase.diary

import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetDiaryEntryForMediaUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(mediaId: Int, mediaType: MediaType): DiaryEntry? {
        return diaryRepository.getDiaryEntryForMedia(mediaId = mediaId, mediaType = mediaType)
    }

    fun observe(mediaId: Int, mediaType: MediaType): Flow<DiaryEntry?> {
        return diaryRepository.getDiaryEntriesForMedia(mediaId = mediaId, mediaType = mediaType)
            .map { it.firstOrNull() }
    }
}
