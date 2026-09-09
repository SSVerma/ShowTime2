package com.ssverma.shared.domain.auth

import com.ssverma.shared.domain.model.auth.TraktAuthState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow

interface TraktAuthProvider {
    val isConnectedFlow: Flow<Boolean>
    val isConnected: Boolean
    val authState: StateFlow<TraktAuthState>
    suspend fun getAccessToken(): String?
    fun startDeviceAuthorization()
    fun cancelAuthorization()
    fun disconnect()
    fun instantMockConnect()
}

