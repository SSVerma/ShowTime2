package com.ssverma.feature.auth.domain.model

sealed interface AuthState {
    object Unauthorized : AuthState

    sealed interface Approval : AuthState {
        data class Needed(
            val approvalUrl: String
        ) : Approval

        object Asked : Approval

        object Granted : Approval

        object Rejected : Approval
    }

    sealed interface Authorized : AuthState {
        object WithoutSession : Authorized

        data class WithSession(
            val sessionId: String
        ) : Authorized
    }
}