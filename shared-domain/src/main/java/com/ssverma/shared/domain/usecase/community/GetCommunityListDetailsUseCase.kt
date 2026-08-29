package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommunityListDetailsUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    operator fun invoke(listId: String): Flow<CommunityCuratedList?> {
        return communityRepository.getCommunityListDetails(listId = listId)
    }
}
