package com.ssverma.shared.domain.auth

import kotlinx.coroutines.flow.Flow

interface TraktAuthProvider {
    val isConnectedFlow: Flow<Boolean>
    val isConnected: Boolean
    suspend fun getAccessToken(): String?
}
