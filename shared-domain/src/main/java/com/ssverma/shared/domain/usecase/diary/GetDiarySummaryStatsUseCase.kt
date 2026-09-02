package com.ssverma.shared.domain.usecase.diary

import com.ssverma.shared.domain.model.diary.DiarySummaryStats
import com.ssverma.shared.domain.repository.DiaryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDiarySummaryStatsUseCase @Inject constructor(
    private val diaryRepository: DiaryRepository
) {
    operator fun invoke(): Flow<DiarySummaryStats> {
        return diaryRepository.getDiarySummaryStats()
    }
}
