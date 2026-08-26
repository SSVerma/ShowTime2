package com.ssverma.feature.auth.domain

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.feature.auth.data.local.TraktAuthStorage
import com.ssverma.feature.auth.data.remote.TraktAuthService
import com.ssverma.feature.auth.domain.model.TraktAuthState
import com.ssverma.feature.auth.domain.model.TraktDeviceCodeResponse
import com.ssverma.feature.auth.domain.model.TraktUser
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class TraktAuthManagerTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val mockTraktAuthService: TraktAuthService = mockk(relaxed = true)
    private val mockTraktAuthStorage: TraktAuthStorage = mockk(relaxed = true)
    private val mockDebugConfigManager: com.ssverma.core.storage.debug.DebugConfigManager =
        mockk(relaxed = true)

    private val storedTokenFlow = MutableStateFlow<String?>(null)
    private val storedUserFlow = MutableStateFlow<TraktUser?>(null)
    private val isMockTraktFlow = MutableStateFlow(false)
    private val customClientIdFlow = MutableStateFlow("")

    private lateinit var traktAuthManager: TraktAuthManager

    @Before
    fun setUp() {
        coEvery { mockTraktAuthStorage.accessTokenFlow } returns storedTokenFlow
        coEvery { mockTraktAuthStorage.traktUserFlow } returns storedUserFlow
        coEvery { mockDebugConfigManager.isMockTraktEnabled } returns isMockTraktFlow
        coEvery { mockDebugConfigManager.customTraktClientId } returns customClientIdFlow

        traktAuthManager = TraktAuthManager(
            scope = testScope,
            traktAuthService = mockTraktAuthService,
            traktAuthStorage = mockTraktAuthStorage,
            debugConfigManager = mockDebugConfigManager
        )
    }

    @Test
    fun `initial state is Disconnected when storage is empty`() = runTest {
        assertThat(traktAuthManager.authState.value).isEqualTo(TraktAuthState.Disconnected)
    }

    @Test
    fun `initial state is Connected when token and user profile exist in storage`() = runTest {
        val user = TraktUser(
            username = "walter_white",
            displayName = "Walter White",
            isVip = false,
            avatarUrl = null
        )
        storedTokenFlow.value = "mock_access_token"
        storedUserFlow.value = user

        val manager = TraktAuthManager(
            scope = testScope,
            traktAuthService = mockTraktAuthService,
            traktAuthStorage = mockTraktAuthStorage,
            debugConfigManager = mockDebugConfigManager
        )

        val state = manager.authState.value
        assertThat(state).isInstanceOf(TraktAuthState.Connected::class.java)
        val connectedState = state as TraktAuthState.Connected
        assertThat(connectedState.user.username).isEqualTo("walter_white")
        assertThat(connectedState.accessToken).isEqualTo("mock_access_token")
    }

    @Test
    fun `startDeviceAuthorization generates device code and transitions to Authorizing`() =
        runTest {
            val response = TraktDeviceCodeResponse(
                deviceCode = "dev_123456",
                userCode = "ABCD1234",
                verificationUrl = "https://trakt.tv/activate",
                expiresInSeconds = 600,
                intervalSeconds = 5
            )

            coEvery {
                mockTraktAuthService.generateDeviceCode(any())
            } returns ApiResponse.Success(body = response, payload = mockk(relaxed = true))

            traktAuthManager.startDeviceAuthorization()

            val state = traktAuthManager.authState.value
            assertThat(state).isInstanceOf(TraktAuthState.Authorizing::class.java)
            val authorizingState = state as TraktAuthState.Authorizing
            assertThat(authorizingState.userCode).isEqualTo("ABCD1234")
            assertThat(authorizingState.verificationUrl).isEqualTo("https://trakt.tv/activate")
        }

    @Test
    fun `disconnect clears local tokens and updates state to Disconnected`() = runTest {
        traktAuthManager.disconnect()
        coVerify { mockTraktAuthStorage.clear() }
        assertThat(traktAuthManager.authState.value).isEqualTo(TraktAuthState.Disconnected)
    }
}
