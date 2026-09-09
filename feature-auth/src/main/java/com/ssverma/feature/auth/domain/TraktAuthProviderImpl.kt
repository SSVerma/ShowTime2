package com.ssverma.feature.auth.domain

import com.ssverma.shared.domain.auth.TraktAuthProvider
import com.ssverma.shared.domain.model.auth.TraktAuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktAuthProviderImpl @Inject constructor(
    private val traktAuthManager: TraktAuthManager
) : TraktAuthProvider {

    override val isConnectedFlow: Flow<Boolean>
        get() = traktAuthManager.authState.map { it is TraktAuthState.Connected }

    override val isConnected: Boolean
        get() = traktAuthManager.authState.value is TraktAuthState.Connected

    override val authState: StateFlow<TraktAuthState>
        get() = traktAuthManager.authState

    override suspend fun getAccessToken(): String? {
        return (traktAuthManager.authState.value as? TraktAuthState.Connected)?.accessToken
    }

    override fun startDeviceAuthorization() {
        traktAuthManager.startDeviceAuthorization()
    }

    override fun cancelAuthorization() {
        traktAuthManager.cancelAuthorization()
    }

    override fun disconnect() {
        traktAuthManager.disconnect()
    }

    override fun instantMockConnect() {
        traktAuthManager.instantMockConnect()
    }
}
