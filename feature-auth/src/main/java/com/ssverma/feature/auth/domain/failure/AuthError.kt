package com.ssverma.feature.auth.domain.failure

import com.ssverma.shared.domain.failure.Failure
import com.ssverma.shared.domain.model.auth.AuthState

data class AuthError(
    val authState: AuthState,
    val failure: Failure.CoreFailure
)