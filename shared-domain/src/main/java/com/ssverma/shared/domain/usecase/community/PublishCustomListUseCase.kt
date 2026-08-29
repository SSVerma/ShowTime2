package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.community.PublishCustomListParams
import com.ssverma.shared.domain.repository.CommunityRepository
import com.ssverma.shared.domain.repository.LibraryRepository
import javax.inject.Inject

class PublishCustomListUseCase @Inject constructor(
    private val communityRepository: CommunityRepository,
    private val libraryRepository: LibraryRepository
) {
    suspend operator fun invoke(params: PublishCustomListParams): Result<Unit, Failure.CoreFailure> {
        val result = communityRepository.publishCustomList(params = params)
        if (result is Result.Success) {
            libraryRepository.setCustomListPublicStatus(
                listId = params.localList.listId,
                isPublic = true
            )
        }
        return result
    }
}
