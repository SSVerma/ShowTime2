package com.ssverma.feature.account.data.mapper

import com.ssverma.api.service.tmdb.convertToFullTmdbImageUrl
import com.ssverma.api.service.tmdb.response.AccountPayload
import com.ssverma.feature.account.domain.model.Profile
import com.ssverma.shared.data.mapper.Mapper
import com.ssverma.shared.data.mapper.MultiMapper
import com.ssverma.showtime.UserAccount
import javax.inject.Inject

class ProfileMapper @Inject constructor() : MultiMapper<AccountPayload, UserAccount, Profile?>() {
    override suspend fun mapRemote(remote: AccountPayload): Profile {
        return remote.asProfile()
    }

    override suspend fun mapLocal(local: UserAccount): Profile? {
        return local.asProfileOrNull()
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

private fun UserAccount.asProfileOrNull(): Profile? {
    return Profile(
        id = accountId,
        userName = username,
        displayName = displayName,
        imageUrl = imageUrl
    ).takeIf { accountId != 0 }
}