package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBlockedCommunityUsersUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    operator fun invoke(): Flow<Set<String>> {
        return communityRepository.getBlockedUserIdsFlow()
    }
}
