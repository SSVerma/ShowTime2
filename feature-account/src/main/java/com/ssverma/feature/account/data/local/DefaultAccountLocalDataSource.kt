package com.ssverma.feature.account.data.local

import com.ssverma.core.storage.typedobject.ObjectStorage
import com.ssverma.showtime.UserAccount
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class DefaultAccountLocalDataSource @Inject constructor(
    private val userAccountStorage: ObjectStorage<UserAccount>
) : AccountLocalDataSource {
    override suspend fun loadUserAccount(): UserAccount {
        return userAccountStorage.data.first()
    }

    override suspend fun persistUserAccount(
        accountId: Int,
        userName: String,
        displayName: String,
        imageUrl: String?
    ) {
        userAccountStorage.updateData { currentUserAccount ->
            currentUserAccount.toBuilder()
                .setAccountId(accountId)
                .setUsername(userName)
                .setDisplayName(displayName)
                .setImageUrl(imageUrl.orEmpty())
                .build()
        }
    }

    override suspend fun clearUserAccount() {
        userAccountStorage.updateData { currentUserAccount ->
            currentUserAccount.toBuilder().clear().build()
        }
    }
}