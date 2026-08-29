package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetCommunityListsUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    operator fun invoke(category: String? = null): Flow<List<CommunityCuratedList>> {
        return communityRepository.getCommunityCuratedLists(category = category)
    }
}
