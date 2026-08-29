package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.community.CloneCommunityListParams
import com.ssverma.shared.domain.repository.CommunityRepository
import com.ssverma.shared.domain.repository.LibraryRepository
import javax.inject.Inject

class CloneCommunityListUseCase @Inject constructor(
    private val libraryRepository: LibraryRepository,
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(params: CloneCommunityListParams): Result<String, Failure.CoreFailure> {
        return try {
            val localListId =
                libraryRepository.cloneCommunityListToLocal(communityList = params.communityList)
            communityRepository.recordListClone(listId = params.communityList.listId)
            Result.Success(localListId)
        } catch (e: Exception) {
            Result.Error(Failure.CoreFailure.UnexpectedFailure)
        }
    }
}
