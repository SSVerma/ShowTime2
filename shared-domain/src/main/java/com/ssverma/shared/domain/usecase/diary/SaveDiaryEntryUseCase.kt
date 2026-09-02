package com.ssverma.shared.domain.usecase.diary

import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.repository.DiaryRepository
import javax.inject.Inject

class SaveDiaryEntryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(entry: DiaryEntry): Long {
        return diaryRepository.saveDiaryEntry(entry)
    }
}
