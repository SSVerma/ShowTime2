package com.ssverma.feature.auth.ui.auth

import com.ssverma.feature.auth.domain.failure.AuthError
import com.ssverma.feature.auth.domain.model.AuthState

data class AuthUiState(
    val authState: AuthState? = null,
    val loading: Boolean = false,
    val error: AuthError? = null
)
