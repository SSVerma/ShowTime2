package com.ssverma.shared.domain.usecase.diary

import com.ssverma.shared.domain.repository.DiaryRepository
import javax.inject.Inject

class DeleteDiaryEntryUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    suspend operator fun invoke(id: Long) {
        diaryRepository.deleteDiaryEntry(id)
    }
}
