package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.community.UnpublishCustomListParams
import com.ssverma.shared.domain.repository.CommunityRepository
import com.ssverma.shared.domain.repository.LibraryRepository
import javax.inject.Inject

class UnpublishCustomListUseCase @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(params: UnpublishCustomListParams): Result<Unit, Failure.CoreFailure> {
        val result = communityRepository.unpublishCustomList(params = params)
        if (result is Result.Success) {
            libraryRepository.setCustomListPublicStatus(listId = params.listId, isPublic = false)
        }
        return result
    }
}
