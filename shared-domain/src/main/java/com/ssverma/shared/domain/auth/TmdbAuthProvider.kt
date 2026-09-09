package com.ssverma.shared.domain.auth

import com.ssverma.shared.domain.model.auth.AuthState
import kotlinx.coroutines.flow.Flow

interface TmdbAuthProvider {
    val authFlow: Flow<AuthState>
    suspend fun getSessionId(): String?
    fun logout()
}
