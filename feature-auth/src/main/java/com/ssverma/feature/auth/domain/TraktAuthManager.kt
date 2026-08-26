package com.ssverma.feature.auth.domain

import com.ssverma.core.di.AppScoped
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.storage.debug.DebugConfigManager
import com.ssverma.feature.auth.data.local.TraktAuthStorage
import com.ssverma.feature.auth.data.remote.TraktAuthService
import com.ssverma.feature.auth.domain.defaults.TraktDefaults
import com.ssverma.feature.auth.domain.model.TraktAuthState
import com.ssverma.feature.auth.domain.model.TraktDeviceCodeRequest
import com.ssverma.feature.auth.domain.model.TraktDeviceTokenRequest
import com.ssverma.feature.auth.domain.model.TraktTokenResponse
import com.ssverma.feature.auth.domain.model.TraktUser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TraktAuthManager @Inject constructor(
    @AppScoped private val scope: CoroutineScope,
    private val traktAuthService: TraktAuthService,
    private val traktAuthStorage: TraktAuthStorage,
    private val debugConfigManager: DebugConfigManager
) {
    private val _authState = MutableStateFlow<TraktAuthState>(TraktAuthState.Disconnected)
    val authState: StateFlow<TraktAuthState> = _authState.asStateFlow()

    private var authorizationJob: Job? = null

    init {
        scope.launch {
            combine(
                traktAuthStorage.accessTokenFlow,
                traktAuthStorage.traktUserFlow
            ) { token, user ->
                if (!token.isNullOrBlank() && user != null) {
                    TraktAuthState.Connected(user = user, accessToken = token)
                } else {
                    TraktAuthState.Disconnected
                }
            }.collect { state ->
                // Only update if not currently in an active authorization flow
                if (_authState.value !is TraktAuthState.Authorizing) {
                    _authState.value = state
                }
            }
        }
    }

    fun startDeviceAuthorization() {
        authorizationJob?.cancel()
        authorizationJob = scope.launch {
            _authState.value = TraktAuthState.Disconnected

            // --- Mock Trakt Mode (Enabled in Debug for instant testing) ---
            if (debugConfigManager.isMockTraktEnabled.value) {
                _authState.value = TraktAuthState.Authorizing(
                    userCode = "SHOW2026",
                    verificationUrl = "https://trakt.tv/activate",
                    secondsRemaining = 600
                )

                // Simulate realistic approval countdown (3 seconds)
                for (i in 1..3) {
                    delay(1000)
                    _authState.value = TraktAuthState.Authorizing(
                        userCode = "SHOW2026",
                        verificationUrl = "https://trakt.tv/activate",
                        secondsRemaining = 600 - i
                    )
                }

                // Complete Mock Connection
                instantMockConnect()
                return@launch
            }

            // --- Live Trakt API Connection ---
            val activeClientId = debugConfigManager.customTraktClientId.value.ifBlank {
                TraktDefaults.DefaultClientId
            }
            val request = TraktDeviceCodeRequest(clientId = activeClientId)

            when (val result = traktAuthService.generateDeviceCode(request)) {
                is ApiResponse.Success -> {
                    val codeResponse = result.body
                    var remainingSeconds = codeResponse.expiresInSeconds
                    val interval =
                        (if (codeResponse.intervalSeconds > 0) codeResponse.intervalSeconds else 5)

                    _authState.value = TraktAuthState.Authorizing(
                        userCode = codeResponse.userCode,
                        verificationUrl = codeResponse.verificationUrl,
                        secondsRemaining = remainingSeconds
                    )

                    // Polling loop
                    var pollCounter = 0
                    while (remainingSeconds > 0) {
                        delay(1000)
                        remainingSeconds--
                        pollCounter++

                        _authState.value = TraktAuthState.Authorizing(
                            userCode = codeResponse.userCode,
                            verificationUrl = codeResponse.verificationUrl,
                            secondsRemaining = remainingSeconds
                        )

                        if (pollCounter >= interval) {
                            pollCounter = 0
                            val tokenResult = traktAuthService.pollDeviceToken(
                                TraktDeviceTokenRequest(
                                    deviceCode = codeResponse.deviceCode,
                                    clientId = TraktDefaults.DefaultClientId
                                )
                            )

                            when (tokenResult) {
                                is ApiResponse.Success -> {
                                    val token = tokenResult.body
                                    traktAuthStorage.saveTokens(token)

                                    // Fetch User profile
                                    val userProfileResult = traktAuthService.getCurrentUserProfile(
                                        bearerToken = "Bearer ${token.accessToken}",
                                        clientId = TraktDefaults.DefaultClientId
                                    )

                                    val traktUser = if (userProfileResult is ApiResponse.Success) {
                                        val payload = userProfileResult.body
                                        TraktUser(
                                            username = payload.username,
                                            displayName = payload.name?.ifBlank { payload.username }
                                                ?: payload.username,
                                            isVip = payload.isVip,
                                            avatarUrl = payload.images?.avatar?.fullUrl
                                        )
                                    } else {
                                        TraktUser(
                                            username = "Trakt Cinephile",
                                            displayName = "Trakt Cinephile",
                                            isVip = false,
                                            avatarUrl = null
                                        )
                                    }

                                    traktAuthStorage.saveUserProfile(traktUser)
                                    _authState.value = TraktAuthState.Connected(
                                        user = traktUser,
                                        accessToken = token.accessToken
                                    )
                                    return@launch
                                }

                                is ApiResponse.Error.ClientError -> {
                                    val httpCode = tokenResult.payload.httpCode
                                    when (httpCode) {
                                        400 -> {
                                            // Pending - user has not approved yet, continue loop
                                        }

                                        404 -> {
                                            _authState.value =
                                                TraktAuthState.Error("Invalid device code.")
                                            return@launch
                                        }

                                        409 -> {
                                            _authState.value =
                                                TraktAuthState.Error("Code already approved.")
                                            return@launch
                                        }

                                        410 -> {
                                            _authState.value =
                                                TraktAuthState.Error("Device code expired.")
                                            return@launch
                                        }

                                        429 -> {
                                            // Slow down by increasing poll interval
                                            pollCounter = -5
                                        }

                                        else -> {
                                            // Keep waiting for transient client errors
                                        }
                                    }
                                }

                                else -> {
                                    // Transient network or server error, continue polling
                                }
                            }
                        }
                    }

                    _authState.value =
                        TraktAuthState.Error("Authorization timed out. Please try again.")
                }

                is ApiResponse.Error -> {
                    _authState.value =
                        TraktAuthState.Error("Unable to reach Trakt.tv. Please check your connection.")
                }
            }
        }
    }

    fun cancelAuthorization() {
        authorizationJob?.cancel()
        authorizationJob = null
        scope.launch {
            val user = traktAuthStorage.traktUserFlow
            val token = traktAuthStorage.getAccessToken()
            // Reset to connected or disconnected
            if (!token.isNullOrBlank()) {
                // Keep connected
            } else {
                _authState.value = TraktAuthState.Disconnected
            }
        }
    }

    fun disconnect() {
        authorizationJob?.cancel()
        authorizationJob = null
        scope.launch {
            traktAuthStorage.clear()
            _authState.value = TraktAuthState.Disconnected
        }
    }

    fun instantMockConnect() {
        authorizationJob?.cancel()
        authorizationJob = null
        scope.launch {
            val mockToken = TraktTokenResponse(
                accessToken = "mock_trakt_token_dev",
                expiresIn = 7776000,
                refreshToken = "mock_trakt_refresh_dev"
            )
            val mockUser = TraktUser(
                username = "cinephile_dev",
                displayName = "ShowTime Developer",
                isVip = true,
                avatarUrl = null
            )
            traktAuthStorage.saveTokens(mockToken)
            traktAuthStorage.saveUserProfile(mockUser)
            _authState.value =
                TraktAuthState.Connected(user = mockUser, accessToken = mockToken.accessToken)
        }
    }
}
