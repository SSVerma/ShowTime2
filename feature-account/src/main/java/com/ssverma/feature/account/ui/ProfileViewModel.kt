package com.ssverma.feature.account.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ssverma.feature.account.domain.repository.AccountRepository
import com.ssverma.feature.auth.domain.AuthManager
import com.ssverma.feature.auth.domain.model.AuthState
import com.ssverma.feature.auth.domain.sessionIdOrNull
import com.ssverma.shared.domain.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _profileUiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val profileUiState = _profileUiState.asStateFlow()

    init {
        viewModelScope.launch {
            authManager.authFlow.collectLatest { authState ->
                if (authState is AuthState.Authorized.WithSession) {
                    fetchProfile()
                }

                if (authState is AuthState.Unauthorized) {
                    accountRepository.removeUserAccount()
                }
            }
        }
    }

    fun fetchProfile() {
        _profileUiState.value = ProfileUiState.Loading

        viewModelScope.launch {
            val profileResult = accountRepository.fetchProfile(
                sessionId = authManager.sessionIdOrNull().orEmpty()
            )
            _profileUiState.value = when (profileResult) {
                is Result.Error -> {
                    ProfileUiState.Error(failure = profileResult.error)
                }
                is Result.Success -> {
                    ProfileUiState.ShowProfileContent(profile = profileResult.data)
                }
            }
        }
    }

    fun logout() {
        _profileUiState.value = ProfileUiState.Loading
        authManager.logout()
    }
}