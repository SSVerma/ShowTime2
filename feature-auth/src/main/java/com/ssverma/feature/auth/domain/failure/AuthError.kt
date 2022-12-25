package com.ssverma.feature.auth.domain.failure

import com.ssverma.feature.auth.domain.model.AuthState
import com.ssverma.shared.domain.failure.Failure

data class AuthError(
    val authState: AuthState,
    val failure: Failure.CoreFailure
)