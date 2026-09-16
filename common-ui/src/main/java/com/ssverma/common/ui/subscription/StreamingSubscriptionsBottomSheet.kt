package com.ssverma.common.ui.subscription

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.common.ui.subscription.component.MultiServiceGateDialog
import com.ssverma.common.ui.subscription.component.StreamingTierContextBanner
import com.ssverma.common.ui.subscription.component.SubscriptionProviderItem
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.ui.R
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamingSubscriptionsBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    onUpgradeToPro: () -> Unit = {},
    viewModel: StreamingSubscriptionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is StreamingSubscriptionsUiEffect.SubscriptionsSaved -> {
                    onDismissRequest()
                }

                is StreamingSubscriptionsUiEffect.ShowMultiServiceGate -> {
                    // Dialog shown via uiState.showMultiServiceGate
                }
            }
        }
    }

    ShowTimeBottomSheet(
        onDismissRequest = onDismissRequest,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        modifier = modifier
    ) {
        StreamingSubscriptionsContent(
            uiState = uiState,
            onProviderToggle = { viewModel.toggleProvider(it) },
            onSearchQueryChanged = { viewModel.onSearchQueryChanged(it) },
            onClearAll = { viewModel.clearAll() },
            onSave = { viewModel.saveSubscriptions() },
            onDismiss = onDismissRequest
        )
    }

    if (uiState.showMultiServiceGate) {
        MultiServiceGateDialog(
            isProPaymentEnabled = uiState.isProPaymentEnabled,
            onDismiss = { viewModel.dismissMultiServiceGate() },
            onUpgradeToPro = {
                viewModel.dismissMultiServiceGate()
                onDismissRequest()
                onUpgradeToPro()
            },
            onWatchAd = { activity ->
                viewModel.watchAdForMultiServicePass(activity)
            }
        )
    }
}

@Composable
private fun StreamingSubscriptionsContent(
    uiState: StreamingSubscriptionsUiState,
    onProviderToggle: (Int) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onClearAll: () -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val allProviders = (uiState.providersState as? UiState.Success)?.data.orEmpty()
    val filteredProviders = remember(allProviders, uiState.searchQuery) {
        if (uiState.searchQuery.isBlank()) {
            allProviders
        } else {
            allProviders.filter {
                it.providerName.contains(uiState.searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .imePadding()
            .padding(top = MaterialTheme.spacing.small)
    ) {
        // Header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stringResource(R.string.streaming_subscriptions_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.streaming_subscriptions_desc),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (uiState.selectedProviderIds.isNotEmpty()) {
                TextButton(onClick = onClearAll) {
                    Text(
                        text = stringResource(R.string.streaming_subscriptions_clear_all),
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }

        // Tier / Pro / Pass Context Banner (collapses when searching to maximize result area)
        AnimatedVisibility(
            visible = uiState.searchQuery.isBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
                StreamingTierContextBanner(
                    isProActive = uiState.isProActive,
                    isPassActive = uiState.isPassActive,
                    modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Search Bar (pinned in view above results)
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = {
                Text(
                    text = stringResource(R.string.streaming_subscriptions_search_hint),
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Search,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (uiState.searchQuery.isNotBlank()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(
                            imageVector = Icons.Rounded.Clear,
                            contentDescription = stringResource(R.string.close),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = MaterialTheme.colorScheme.surface,
                unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Providers List Container - uses weight(1f, fill = false) so it contracts above keyboard and CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = false)
        ) {
            when (uiState.providersState) {
                is UiState.Loading -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    ) {
                        ShowTimeLoadingIndicator()
                    }
                }

                is UiState.Error -> {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp)
                            .padding(horizontal = MaterialTheme.spacing.medium)
                    ) {
                        Text(
                            text = stringResource(R.string.unexpected_error_msg),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                is UiState.Success -> {
                    if (filteredProviders.isEmpty()) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(horizontal = MaterialTheme.spacing.medium)
                        ) {
                            Text(
                                text = stringResource(R.string.streaming_subscriptions_empty_services),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            contentPadding = PaddingValues(
                                horizontal = MaterialTheme.spacing.medium,
                                vertical = MaterialTheme.spacing.extraSmall
                            ),
                            verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 420.dp)
                        ) {
                            items(
                                items = filteredProviders,
                                key = { it.providerId },
                                contentType = { "provider_item" }
                            ) { provider ->
                                val isSelected =
                                    uiState.selectedProviderIds.contains(provider.providerId)
                                SubscriptionProviderItem(
                                    provider = provider,
                                    isSelected = isSelected,
                                    onClick = { onProviderToggle(provider.providerId) }
                                )
                            }
                        }
                    }
                }

                is UiState.Idle -> Unit
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // Bottom CTA Bar
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(
                        horizontal = MaterialTheme.spacing.medium,
                        vertical = MaterialTheme.spacing.small
                    )
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(text = stringResource(R.string.cancel))
                }

                Button(
                    onClick = onSave,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.weight(2f)
                ) {
                    val count = uiState.selectedProviderIds.size
                    Text(
                        text = if (count > 0) {
                            stringResource(R.string.streaming_subscriptions_save_count, count)
                        } else {
                            stringResource(R.string.streaming_subscriptions_save)
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
