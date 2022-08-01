package com.ssverma.feature.account.data.mapper

import com.ssverma.api.service.tmdb.convertToFullTmdbImageUrl
import com.ssverma.api.service.tmdb.response.AccountPayload
import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.shared.data.mapper.Mapper
import javax.inject.Inject

class ProfileMapper @Inject constructor() : Mapper<AccountPayload, Profile>() {
    override suspend fun map(input: AccountPayload): Profile {
        return input.asProfile()
    }
}

private fun AccountPayload.asProfile(): Profile {
    return Profile(
        id = id,
        userName = userName.orEmpty(),
        displayName = displayName.orEmpty(),
        imageUrl = avatar?.avatarPath.convertToFullTmdbImageUrl()
    )
}