package com.ssverma.feature.auth.domain

import com.ssverma.shared.domain.auth.TmdbAuthProvider
import com.ssverma.shared.domain.model.auth.AuthState
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TmdbAuthProviderImpl @Inject constructor(
    private val authManager: AuthManager
) : TmdbAuthProvider {

    override val authFlow: Flow<AuthState>
        get() = authManager.authFlow

    override suspend fun getSessionId(): String? {
        return authManager.sessionIdOrNull()
    }

    override fun logout() {
        authManager.logout()
    }
}
