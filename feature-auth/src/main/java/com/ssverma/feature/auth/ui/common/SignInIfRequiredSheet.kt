package com.ssverma.feature.auth.ui.common

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.ssverma.common.ui.trakt.TraktConnectBottomSheet
import com.ssverma.core.backup.auth.GoogleAuthClient
import com.ssverma.core.ui.util.findActivity
import com.ssverma.feature.auth.domain.AuthSessionManager
import com.ssverma.feature.auth.domain.TraktAuthManager
import com.ssverma.feature.auth.domain.model.AuthProvider

/**
 * Plug-and-Play Authentication Sheet.
 * Call this composable whenever an action requires an active provider session.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignInIfRequiredSheet(
    requiredProvider: AuthProvider,
    authSessionManager: AuthSessionManager,
    traktAuthManager: TraktAuthManager,
    googleAuthClient: GoogleAuthClient,
    onDismiss: () -> Unit,
    onAuthenticated: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isLinked = authSessionManager.isLinked(requiredProvider)

    LaunchedEffect(isLinked) {
        if (isLinked) {
            onAuthenticated()
            onDismiss()
        }
    }

    when (requiredProvider) {
        AuthProvider.TRAKT -> {
            TraktConnectBottomSheet(
                traktAuthProvider = traktAuthManager,
                onDismiss = onDismiss,
                onConnected = {
                    onAuthenticated()
                },
                modifier = modifier
            )
        }

        AuthProvider.GOOGLE -> {
            LaunchedEffect(Unit) {
                val activity = context.findActivity()
                if (activity != null) {
                    val result = googleAuthClient.signIn(activity)
                    if (result.isSuccess) {
                        onAuthenticated()
                    }
                }
                onDismiss()
            }
        }

        AuthProvider.TMDB -> {
            // Legacy / not used in modern flow
            LaunchedEffect(Unit) {
                onDismiss()
            }
        }
    }
}
