package com.ssverma.shared.domain.model.auth

data class TraktUser(
    val username: String,
    val displayName: String,
    val isVip: Boolean,
    val avatarUrl: String?
)

sealed interface TraktAuthState {
    data object Disconnected : TraktAuthState

    data class Authorizing(
        val userCode: String,
        val verificationUrl: String,
        val secondsRemaining: Int
    ) : TraktAuthState

    data class Connected(
        val user: TraktUser,
        val accessToken: String
    ) : TraktAuthState

    data class Error(
        val message: String
    ) : TraktAuthState
}
