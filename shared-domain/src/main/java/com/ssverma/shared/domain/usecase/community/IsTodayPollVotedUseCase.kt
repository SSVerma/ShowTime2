package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

/**
 * Checks whether the current user has already voted in today's Daily Poll.
 * Resolves from local storage without network overhead to preserve Firestore Spark quota.
 */
class IsTodayPollVotedUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    operator fun invoke(date: LocalDate = LocalDate.now()): Flow<Boolean> {
        return communityRepository.isTodayPollVotedFlow(date = date)
    }
}
