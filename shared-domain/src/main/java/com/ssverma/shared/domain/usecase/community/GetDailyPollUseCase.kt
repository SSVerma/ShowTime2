package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.model.community.DailyPoll
import com.ssverma.shared.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject

class GetDailyPollUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    operator fun invoke(
        date: LocalDate = LocalDate.now(),
        forceRefresh: Boolean = false
    ): Flow<DailyPoll> {
        return communityRepository.getDailyPoll(date = date, forceRefresh = forceRefresh)
    }
}
