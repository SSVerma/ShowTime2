package com.ssverma.feature.auth.domain.failure

import com.ssverma.shared.domain.model.auth.AuthState
import com.ssverma.shared.domain.failure.Failure

data class AuthError(
    val authState: AuthState,
    val failure: Failure.CoreFailure
)