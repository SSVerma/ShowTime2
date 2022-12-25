package com.ssverma.feature.auth.domain

import com.ssverma.api.service.tmdb.TmdbDefaults
import com.ssverma.core.di.AppScoped
import com.ssverma.feature.auth.domain.failure.AuthError
import com.ssverma.feature.auth.domain.model.AuthState
import com.ssverma.feature.auth.domain.repository.AuthRepository
import com.ssverma.shared.domain.Result
import com.ssverma.shared.domain.failure.Failure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthManager @Inject constructor(
    @AppScoped
    private val externalScope: CoroutineScope,
    private val authRepository: AuthRepository
) {
    private val _authProcessFlow = MutableSharedFlow<Result<AuthState, AuthError>>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    internal val authProcessFlow: Flow<Result<AuthState, AuthError>> =
        _authProcessFlow.asSharedFlow()

    private val _authFlow = MutableSharedFlow<AuthState>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )

    val authFlow: Flow<AuthState> = _authFlow.asSharedFlow()

    init {
        externalScope.launch {
            val authorized = authRepository.isAuthorized()
            if (authorized) {
                val authState = when (val sessionIdResult = authRepository.fetchSessionId()) {
                    is Result.Error -> {
                        AuthState.Authorized.WithoutSession
                    }
                    is Result.Success -> {
                        AuthState.Authorized.WithSession(sessionId = sessionIdResult.data)
                    }
                }
                _authProcessFlow.tryEmit(Result.Success(data = authState))
                _authFlow.tryEmit(authState)
            } else {
                _authProcessFlow.tryEmit(Result.Success(data = AuthState.Unauthorized))
                _authFlow.tryEmit(AuthState.Unauthorized)
            }
        }
    }

    internal fun authenticate() {
        externalScope.launch {
            when (val requestTokenResult = authRepository.fetchRequestToken()) {
                is Result.Error -> {
                    _authProcessFlow.tryEmit(
                        Result.Error(
                            error = AuthError(
                                authState = AuthState.Unauthorized,
                                failure = requestTokenResult.error
                            )
                        )
                    )
                }
                is Result.Success -> {
                    val authApprovalUrl = TmdbDefaults.authApprovalRedirectUrl(
                        requestToken = requestTokenResult.data
                    )
                    _authProcessFlow.tryEmit(
                        Result.Success(
                            data = AuthState.Approval.Needed(approvalUrl = authApprovalUrl)
                        )
                    )
                    _authFlow.tryEmit(AuthState.Approval.Needed(approvalUrl = authApprovalUrl))
                }
            }
        }
    }

    internal fun authorize() {
        externalScope.launch {
            when (val accessTokenResult = authRepository.fetchAccessToken()) {
                is Result.Error -> {
                    when (accessTokenResult.error) {
                        Failure.CoreFailure.NetworkFailure -> {
                            _authProcessFlow.tryEmit(
                                Result.Error(
                                    error = AuthError(
                                        authState = AuthState.Approval.Granted,
                                        failure = accessTokenResult.error
                                    )
                                )
                            )
                        }
                        Failure.CoreFailure.ServiceFailure,
                        Failure.CoreFailure.UnexpectedFailure -> {
                            _authProcessFlow.tryEmit(Result.Success(data = AuthState.Unauthorized))
                            _authFlow.tryEmit(AuthState.Unauthorized)
                        }
                    }
                }
                is Result.Success -> {
                    createSession()
                }
            }
        }
    }

    internal fun createSession() {
        externalScope.launch {
            when (val sessionIdResult = authRepository.fetchSessionId()) {
                is Result.Error -> {
                    _authProcessFlow.tryEmit(
                        Result.Error(
                            error = AuthError(
                                authState = AuthState.Authorized.WithoutSession,
                                failure = sessionIdResult.error
                            )
                        )
                    )
                    _authFlow.tryEmit(AuthState.Authorized.WithoutSession)
                }
                is Result.Success -> {
                    _authProcessFlow.tryEmit(
                        Result.Success(
                            data = AuthState.Authorized.WithSession(
                                sessionId = sessionIdResult.data
                            )
                        )
                    )
                    _authFlow.tryEmit(
                        AuthState.Authorized.WithSession(sessionId = sessionIdResult.data)
                    )
                }
            }
        }
    }

    internal fun updateApprovalStatus(approval: AuthState.Approval) {
        _authProcessFlow.tryEmit(Result.Success(data = approval))

        when (approval) {
            AuthState.Approval.Asked -> {
                // no op
            }
            AuthState.Approval.Granted -> {
                authorize()
            }
            is AuthState.Approval.Needed -> {
                // no op
            }
            AuthState.Approval.Rejected -> {
                _authProcessFlow.tryEmit(Result.Success(data = AuthState.Unauthorized))
                _authFlow.tryEmit(AuthState.Unauthorized)
            }
        }
    }

    fun logout() {
        externalScope.launch {
            when (val logoutResult = authRepository.logout()) {
                is Result.Error -> {
                    val authState = when (val sessionIdResult = authRepository.fetchSessionId()) {
                        is Result.Error -> {
                            AuthState.Authorized.WithoutSession
                        }
                        is Result.Success -> {
                            AuthState.Authorized.WithSession(sessionId = sessionIdResult.data)
                        }
                    }
                    _authProcessFlow.tryEmit(
                        Result.Error(
                            error = AuthError(
                                authState = authState,
                                failure = logoutResult.error
                            )
                        )
                    )
                }
                is Result.Success -> {
                    _authProcessFlow.tryEmit(Result.Success(data = AuthState.Unauthorized))
                    _authFlow.tryEmit(AuthState.Unauthorized)
                }
            }
        }
    }
}

suspend fun AuthManager.sessionIdOrNull(): String? {
    return if (authFlow.firstOrNull() is AuthState.Authorized.WithSession) {
        (authFlow.firstOrNull() as AuthState.Authorized.WithSession).sessionId
    } else null
}