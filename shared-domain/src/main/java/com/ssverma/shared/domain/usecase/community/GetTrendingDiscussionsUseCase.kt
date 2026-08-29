package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.model.community.TrendingDiscussion
import com.ssverma.shared.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetTrendingDiscussionsUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    operator fun invoke(): Flow<List<TrendingDiscussion>> {
        return communityRepository.getTrendingDiscussions()
    }
}
