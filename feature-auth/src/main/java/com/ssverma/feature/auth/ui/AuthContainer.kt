package com.ssverma.feature.auth.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.ssverma.feature.auth.domain.AuthManager
import com.ssverma.feature.auth.domain.model.AuthState

@Composable
fun AuthScreenContainer(
    modifier: Modifier = Modifier,
    onBackPressed: () -> Unit,
    authorizedContent: @Composable (session: AuthState.Authorized.WithSession) -> Unit
) {
    AuthScreen(
        onAuthSessionEstablished = {},
        onBackPressed = onBackPressed,
        authorizedContent = authorizedContent,
        modifier = modifier
    )
}

@Composable
fun AuthContainer(
    unAuthorizedContent: @Composable () -> Unit,
    authorizedContent: @Composable (AuthState.Authorized.WithSession) -> Unit
) {
    AuthContainerInternal(
        unAuthorizedContent = unAuthorizedContent,
        authorizedContent = authorizedContent
    )
}

@Composable
private fun AuthContainerInternal(
    authManager: AuthManager = authManager(),
    unAuthorizedContent: @Composable () -> Unit,
    authorizedContent: @Composable (AuthState.Authorized.WithSession) -> Unit
) {
    val authState by authManager.authFlow.collectAsState(initial = null)

    when (authState) {
        is AuthState.Authorized.WithSession -> {
            authorizedContent(authState as AuthState.Authorized.WithSession)
        }
        else -> {
            unAuthorizedContent()
        }
    }
}