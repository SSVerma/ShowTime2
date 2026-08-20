package com.ssverma.feature.auth.domain

import com.ssverma.core.backup.auth.GoogleAuthClient
import com.ssverma.core.di.AppScoped
import com.ssverma.feature.auth.domain.model.AuthAccount
import com.ssverma.feature.auth.domain.model.AuthProvider
import com.ssverma.feature.auth.domain.model.TraktAuthState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Unified Session Manager coordinating all authentication providers (Google, Trakt, etc.)
 */
@Singleton
class AuthSessionManager @Inject constructor(
    @AppScoped private val scope: CoroutineScope,
    private val googleAuthClient: GoogleAuthClient,
    private val traktAuthManager: TraktAuthManager
) {
    val accounts: StateFlow<Map<AuthProvider, AuthAccount?>> = combine(
        googleAuthClient.currentUser,
        traktAuthManager.authState
    ) { googleUser, traktState ->
        val map = mutableMapOf<AuthProvider, AuthAccount?>()

        if (googleUser != null) {
            map[AuthProvider.GOOGLE] = AuthAccount(
                provider = AuthProvider.GOOGLE,
                id = googleUser.email,
                displayName = googleUser.displayName,
                email = googleUser.email,
                avatarUrl = googleUser.photoUrl,
                isLinked = true
            )
        } else {
            map[AuthProvider.GOOGLE] = null
        }

        if (traktState is TraktAuthState.Connected) {
            map[AuthProvider.TRAKT] = AuthAccount(
                provider = AuthProvider.TRAKT,
                id = traktState.user.username,
                displayName = traktState.user.displayName,
                avatarUrl = traktState.user.avatarUrl,
                isLinked = true
            )
        } else {
            map[AuthProvider.TRAKT] = null
        }

        map
    }.stateIn(
        scope = scope,
        started = SharingStarted.Eagerly,
        initialValue = emptyMap()
    )

    fun isLinked(provider: AuthProvider): Boolean {
        return accounts.value[provider]?.isLinked == true
    }

    fun getAccount(provider: AuthProvider): AuthAccount? {
        return accounts.value[provider]
    }

    suspend fun disconnect(provider: AuthProvider) {
        when (provider) {
            AuthProvider.GOOGLE -> googleAuthClient.signOut()
            AuthProvider.TRAKT -> traktAuthManager.disconnect()
            AuthProvider.TMDB -> {}
        }
    }
}
