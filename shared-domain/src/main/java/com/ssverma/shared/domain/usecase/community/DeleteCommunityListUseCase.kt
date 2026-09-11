package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.repository.CommunityRepository
import javax.inject.Inject

class DeleteCommunityListUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(listId: String): Result<Unit, Failure.CoreFailure> {
        return communityRepository.deleteCommunityList(listId = listId)
    }
}
