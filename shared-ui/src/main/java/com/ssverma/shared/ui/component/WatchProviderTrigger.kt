package com.ssverma.shared.ui.component

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.LiveTv
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.ui.R
import com.ssverma.shared.ui.component.section.WatchProvidersBottomSheet
import com.ssverma.shared.ui.viewmodel.WatchProviderViewModel

enum class WatchProviderTriggerVariant {
    Icon,
    OutlinedButton
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WatchProviderTrigger(
    mediaId: Int,
    isMovie: Boolean,
    modifier: Modifier = Modifier,
    variant: WatchProviderTriggerVariant = WatchProviderTriggerVariant.Icon,
    onWatchProviderClick: (provider: ProviderInfo) -> Unit,
    viewModel: WatchProviderViewModel = hiltViewModel()
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val watchProviderState by viewModel.watchProviderState.collectAsState()

    val tooltipState = rememberTooltipState()

    TooltipBox(
        positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
        tooltip = { PlainTooltip { Text(stringResource(R.string.where_to_watch)) } },
        state = tooltipState
    ) {
        when (variant) {
            WatchProviderTriggerVariant.Icon -> {
                IconButton(
                    modifier = modifier
                        .size(30.dp),
                    onClick = {
                        showBottomSheet = true
                        viewModel.fetchWatchProviders(mediaId = mediaId, isMovie = isMovie)
                    },
                    colors = IconButtonDefaults.iconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LiveTv,
                        contentDescription = stringResource(id = R.string.where_to_watch),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            WatchProviderTriggerVariant.OutlinedButton -> {
                FilledTonalButton(
                    onClick = {
                        showBottomSheet = true
                        viewModel.fetchWatchProviders(mediaId, isMovie)
                    },
                    shape = MaterialTheme.shapes.extraLarge,
                    contentPadding = PaddingValues(horizontal = MaterialTheme.spacing.medium),
                    modifier = modifier
                        .height(34.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LiveTv,
                        contentDescription = null,
                        modifier = Modifier
                            .size(16.dp)
                            .padding(end = MaterialTheme.spacing.small)
                    )
                    Text(
                        text = stringResource(id = R.string.where_to_watch),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }

    if (showBottomSheet) {
        WatchProvidersBottomSheet(
            watchProviderUiState = watchProviderState,
            onDismissRequest = {
                showBottomSheet = false
                viewModel.resetState()
            },
            onWatchProviderClick = onWatchProviderClick,
        )
    }
}
