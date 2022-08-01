package com.ssverma.feature.auth.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.navigation.dispatcher.IntentDispatcher.dispatchBrowserIntent
import com.ssverma.core.ui.ScreenLoadingIndicator
import com.ssverma.core.ui.icon.AppIcons
import com.ssverma.feature.auth.R
import com.ssverma.feature.auth.domain.model.AuthState
import com.ssverma.shared.domain.failure.Failure

@Composable
fun AuthContainer(
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
internal fun AuthScreen(
    modifier: Modifier = Modifier,
    viewModel: AuthViewModel = hiltViewModel(),
    onAuthSessionEstablished: () -> Unit,
    onBackPressed: () -> Unit,
    authorizedContent: (@Composable (session: AuthState.Authorized.WithSession) -> Unit)? = null
) {
    val authUiState by viewModel.authUiState.collectAsState()
    val context = LocalContext.current

    val currentOnAuthSessionEstablished by rememberUpdatedState(onAuthSessionEstablished)

    BackHandler { onBackPressed() }

    Box(
        modifier = modifier
            .statusBarsPadding()
            .fillMaxSize()
    ) {
        IconButton(onClick = onBackPressed) {
            Icon(imageVector = AppIcons.Close, contentDescription = null)
        }

        if (authUiState.loading) {
            ScreenLoadingIndicator()
        }

        if (authUiState.error != null) {
            when (authUiState.error?.failure) {
                Failure.CoreFailure.NetworkFailure -> {
                    AuthErrorIndicator(
                        message = stringResource(id = R.string.network_error),
                        onRetry = {
                            when (authUiState.error?.authState) {
                                AuthState.Approval.Granted -> {
                                    viewModel.startAuthorization()
                                }
                                AuthState.Unauthorized -> {
                                    viewModel.startAuthentication()
                                }
                                is AuthState.Authorized.WithSession -> {
                                    viewModel.logout()
                                }
                                AuthState.Authorized.WithoutSession -> {
                                    viewModel.createSession()
                                }
                                else -> {
                                    // No op
                                }
                            }
                        }
                    )
                }
                else -> {
                    AuthErrorIndicator()
                }
            }
        }

        if (authUiState.authState != null) {
            val authState = authUiState.authState ?: return
            when (authState) {
                is AuthState.Authorized.WithSession -> {
                    authorizedContent?.let { it(authState) } ?: currentOnAuthSessionEstablished()
                }
                AuthState.Authorized.WithoutSession -> {
                    NoSessionContent(
                        onProceedClick = {
                            viewModel.createSession()
                        }
                    )
                }
                AuthState.Unauthorized -> {
                    LoginContent(
                        onTmdbConnectClick = {
                            viewModel.startAuthentication()
                        }
                    )
                }
                is AuthState.Approval.Needed -> {
                    context.dispatchBrowserIntent(
                        webUrl = authState.approvalUrl
                    )
                    viewModel.onApprovalAsked()
                }
                AuthState.Approval.Asked -> {
                    ApprovalAskedContent()
                }
                AuthState.Approval.Granted -> {
                    ApprovalGrantedContent()
                }
                AuthState.Approval.Rejected -> {
                    ApprovalRejectedContent()
                }
            }
        }
    }
}

@Composable
private fun AuthErrorIndicator(
    modifier: Modifier = Modifier,
    message: String = stringResource(id = R.string.something_went_wrong),
    onRetry: (() -> Unit)? = null
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = modifier.fillMaxSize()
    ) {
        Text(text = message)
        Spacer(modifier = Modifier.height(8.dp))
        onRetry?.let {
            OutlinedButton(onClick = it) {
                Text(text = stringResource(R.string.retry))
            }
        }
    }
}