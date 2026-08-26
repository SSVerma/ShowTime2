package com.ssverma.feature.account.ui.trakt

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.ssverma.core.ui.Screen
import com.ssverma.core.ui.asString
import com.ssverma.core.ui.component.showImmediateSnackbar
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.account.R
import com.ssverma.feature.account.ui.pro.ProPaywallBottomSheet
import com.ssverma.feature.auth.domain.model.TraktAuthState
import com.ssverma.feature.auth.ui.trakt.TraktConnectBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraktSyncScreen(
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TraktSyncViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showDisconnectConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.message) {
        uiState.message?.let { msg ->
            snackbarHostState.showImmediateSnackbar(
                message = msg.asString(context),
                duration = SnackbarDuration.Short
            )
            viewModel.clearMessage()
        }
    }

    Screen(
        title = stringResource(id = R.string.trakt_cloud_sync),
        onBackPressed = onBackPressed,
        scrollBehavior = scrollBehavior,
        snackbarHostState = snackbarHostState,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = MaterialTheme.spacing.medium)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            when (val authState = uiState.traktAuthState) {
                is TraktAuthState.Connected -> {
                    TraktConnectedCard(
                        traktUser = authState.user,
                        isSyncing = uiState.isTraktSyncing,
                        isProActive = uiState.isProActive,
                        onSyncNowClick = { viewModel.syncTraktNow() },
                        onDisconnectClick = { showDisconnectConfirmDialog = true },
                        onUpgradeClick = { viewModel.openPaywall() }
                    )
                }

                else -> {
                    TraktDisconnectedCard(
                        isProActive = uiState.isProActive,
                        onConnectClick = {
                            if (uiState.isProActive) {
                                viewModel.openTraktConnect()
                            } else {
                                viewModel.openPaywall()
                            }
                        },
                        onUpgradeClick = { viewModel.openPaywall() }
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
        }

        // Disconnect Confirmation Dialog
        if (showDisconnectConfirmDialog) {
            AlertDialog(
                onDismissRequest = { showDisconnectConfirmDialog = false },
                shape = RoundedCornerShape(24.dp),
                icon = {
                    Icon(
                        imageVector = Icons.Rounded.Tv,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                title = {
                    Text(
                        text = stringResource(R.string.disconnect_trakt_confirm_title),
                        fontWeight = FontWeight.Bold
                    )
                },
                text = {
                    Text(text = stringResource(R.string.disconnect_trakt_confirm_msg))
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showDisconnectConfirmDialog = false
                            viewModel.disconnectTrakt()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    ) {
                        Text(text = stringResource(R.string.disconnect))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDisconnectConfirmDialog = false }) {
                        Text(text = stringResource(R.string.cancel))
                    }
                }
            )
        }

        // Trakt Connect Bottom Sheet
        if (uiState.isTraktConnectSheetVisible) {
            TraktConnectBottomSheet(
                traktAuthManager = viewModel.traktAuthManager,
                onDismiss = { viewModel.closeTraktConnect() },
                onConnected = {
                    viewModel.closeTraktConnect()
                    viewModel.syncTraktNow()
                }
            )
        }

        // Pro Paywall Bottom Sheet
        if (uiState.isPaywallVisible) {
            val activity = context as? android.app.Activity
            ProPaywallBottomSheet(
                products = uiState.availableProducts,
                isProActive = uiState.isProActive,
                isRestoring = uiState.isRestoringPurchases,
                onPurchaseClick = { act, product ->
                    viewModel.purchaseProduct(activity = act, product = product)
                },
                onRestoreClick = { viewModel.restorePurchases() },
                onDismissRequest = { viewModel.dismissPaywall() }
            )
        }
    }
}

@Composable
private fun TraktConnectedCard(
    traktUser: com.ssverma.feature.auth.domain.model.TraktUser,
    isSyncing: Boolean,
    isProActive: Boolean,
    onSyncNowClick: () -> Unit,
    onDisconnectClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (traktUser.avatarUrl != null) {
                    AsyncImage(
                        model = traktUser.avatarUrl,
                        contentDescription = traktUser.username,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Tv,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = traktUser.displayName.ifBlank { traktUser.username },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (traktUser.isVip) {
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.extraSmall))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primaryContainer,
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Text(
                                    text = "VIP",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = stringResource(id = R.string.trakt_connected_as, traktUser.username),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                OutlinedButton(
                    onClick = onDisconnectClick,
                    shape = MaterialTheme.shapes.small
                ) {
                    Text(text = stringResource(R.string.disconnect))
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Text(
                text = stringResource(R.string.trakt_cloud_sync_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Button(
                onClick = {
                    if (isProActive) onSyncNowClick() else onUpgradeClick()
                },
                enabled = !isSyncing,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isSyncing) {
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text(text = stringResource(R.string.trakt_syncing))
                } else {
                    Icon(
                        imageVector = if (isProActive) Icons.Rounded.Sync else Icons.Rounded.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                    Text(
                        text = if (isProActive) stringResource(R.string.trakt_sync_now) else "Upgrade to Pro to Sync"
                    )
                }
            }
        }
    }
}

@Composable
private fun TraktDisconnectedCard(
    isProActive: Boolean,
    onConnectClick: () -> Unit,
    onUpgradeClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedCard(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(MaterialTheme.spacing.medium)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Rounded.Tv,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                Column {
                    Text(
                        text = stringResource(R.string.trakt_cloud_sync),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Scrobble, ratings & progress sync",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Text(
                text = stringResource(R.string.trakt_cloud_sync_desc),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            Button(
                onClick = onConnectClick,
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.CloudSync,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = stringResource(R.string.connect_trakt),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
