package com.ssverma.feature.account.data.local

import com.ssverma.showtime.UserAccount

interface AccountLocalDataSource {
    suspend fun loadUserAccount(): UserAccount

    suspend fun persistUserAccount(
        accountId: Int,
        userName: String,
        displayName: String,
        imageUrl: String?
    )

    suspend fun clearUserAccount()
}