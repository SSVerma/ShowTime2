package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.model.community.CommunityCuratedList
import com.ssverma.shared.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import com.ssverma.shared.domain.model.community.CommunityOptimizationConfig
import javax.inject.Inject

class GetCommunityListsUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    operator fun invoke(
        category: String? = null,
        limit: Int = CommunityOptimizationConfig.DEFAULT_COMMUNITY_LISTS_LIMIT.toInt()
    ): Flow<List<CommunityCuratedList>> {
        return communityRepository.getCommunityCuratedLists(category = category, limit = limit)
    }
}
