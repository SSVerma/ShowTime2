package com.ssverma.feature.auth.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.feature.auth.domain.AuthManager
import com.ssverma.feature.auth.domain.failure.AuthError
import com.ssverma.feature.auth.domain.model.AuthState
import com.ssverma.shared.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    private val _authUiState = MutableStateFlow(AuthUiState())
    val authUiState = _authUiState.asStateFlow()

    private val authState = MutableStateFlow<AuthState>(AuthState.Unauthorized)

    init {
        viewModelScope.launch {
            _authUiState.update { it.copy(loading = true) }

            authManager.authProcessFlow.collectLatest { result ->
                _authUiState.value = when (result) {
                    is Result.Error -> {
                        authState.update { result.error.authState }
                        AuthUiState(
                            error = AuthError(
                                authState = result.error.authState,
                                failure = result.error.failure
                            )
                        )
                    }
                    is Result.Success -> {
                        authState.update { result.data }
                        AuthUiState(authState = result.data)
                    }
                }
            }
        }
    }

    fun startAuthentication() {
        _authUiState.update { AuthUiState(loading = true) }
        authManager.authenticate()
    }

    fun startAuthorization() {
        _authUiState.update { AuthUiState(loading = true) }
        authManager.authorize()
    }

    fun onApprovalAsked() {
        authManager.updateApprovalStatus(AuthState.Approval.Asked)
    }

    private fun onApprovalRejected() {
        authManager.updateApprovalStatus(AuthState.Approval.Rejected)
    }

    fun logout() {
        _authUiState.update { AuthUiState(loading = true) }
        authManager.logout()
    }

    fun createSession() {
        _authUiState.update { AuthUiState(loading = true) }
        authManager.createSession()
    }

    override fun onCleared() {
        if (authState.value == AuthState.Approval.Asked) {
            onApprovalRejected()
        }
        super.onCleared()
    }
}