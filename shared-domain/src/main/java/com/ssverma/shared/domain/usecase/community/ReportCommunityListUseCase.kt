package com.ssverma.shared.domain.usecase.community

import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.community.ReportCommunityListParams
import com.ssverma.shared.domain.repository.CommunityRepository
import javax.inject.Inject

class ReportCommunityListUseCase @Inject constructor(
    private val communityRepository: CommunityRepository
) {
    suspend operator fun invoke(params: ReportCommunityListParams): Result<Unit, Failure.CoreFailure> {
        return communityRepository.reportCommunityList(params)
    }
}
