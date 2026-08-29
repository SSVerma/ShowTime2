package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.model.community.Comment
import com.ssverma.shared.domain.model.community.DiscussionTarget
import com.ssverma.shared.domain.repository.CommunityRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetDiscussionsUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    operator fun invoke(target: DiscussionTarget): Flow<List<Comment>> {
        return communityRepository.getDiscussions(target)
    }
}
