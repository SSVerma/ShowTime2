package com.ssverma.shared.domain.usecase.diary

import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.repository.DiaryRepository
import com.ssverma.shared.domain.usecase.community.SubmitCommunityRatingUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject

class SaveDiaryEntryUseCase private constructor(
    private val diaryRepository: DiaryRepository,
    private val submitCommunityRatingUseCase: SubmitCommunityRatingUseCase?,
    private val isBridgeEnabled: Boolean
) {
    @Inject
    constructor(
        diaryRepository: DiaryRepository,
        submitCommunityRatingUseCase: SubmitCommunityRatingUseCase
    ) : this(
        diaryRepository = diaryRepository,
        submitCommunityRatingUseCase = submitCommunityRatingUseCase,
        isBridgeEnabled = true
    )

    constructor(diaryRepository: DiaryRepository) : this(
        diaryRepository = diaryRepository,
        submitCommunityRatingUseCase = null,
        isBridgeEnabled = false
    )

    suspend operator fun invoke(entry: DiaryEntry): Long {
        val entryId = diaryRepository.saveDiaryEntry(entry)
        val rating = entry.userRating
        if (isBridgeEnabled && rating > 0f && submitCommunityRatingUseCase != null) {
            try {
                withTimeoutOrNull(COMMUNITY_RATING_SYNC_TIMEOUT_MS) {
                    submitCommunityRatingUseCase.invoke(
                        mediaType = entry.mediaType,
                        mediaId = entry.mediaId,
                        rating = rating
                    )
                }
            } catch (cancellationException: CancellationException) {
                throw cancellationException
            } catch (_: Exception) {
                // Non-fatal: local diary entry is already persisted.
            }
        }
        return entryId
    }

    private companion object {
        const val COMMUNITY_RATING_SYNC_TIMEOUT_MS = 2_500L
    }
}
