package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.community.DailyPoll
import com.ssverma.shared.domain.repository.CommunityRepository
import java.time.LocalDate
import javax.inject.Inject

class VoteDailyPollUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(
        date: LocalDate = LocalDate.now(),
        optionIndex: Int
    ): Result<DailyPoll, Failure.CoreFailure> {
        return communityRepository.voteDailyPoll(date = date, optionIndex = optionIndex)
    }
}
