package com.ssverma.feature.account.ui.profile.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ssverma.core.ui.UiState
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.account.R
import com.ssverma.shared.domain.model.Genre
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PreferredGenresBottomSheet(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PreferredGenresViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEffect.collectLatest { effect ->
            when (effect) {
                is PreferredGenresUiEffect.GenresSaved -> {
                    onDismissRequest()
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
        PreferredGenresContent(
            uiState = uiState,
            onGenreToggle = { viewModel.toggleGenre(it) },
            onSave = { viewModel.savePreferences() },
            onDismiss = onDismissRequest
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PreferredGenresContent(
    uiState: PreferredGenresUiState,
    onGenreToggle: (Int) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = MaterialTheme.spacing.medium)
            .padding(bottom = MaterialTheme.spacing.medium)
    ) {
        // Top drag handle & close button row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.MovieFilter,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Text(
                    text = stringResource(R.string.preferred_genres_title),
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = stringResource(R.string.cancel),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

        Text(
            text = stringResource(R.string.preferred_genres_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Status / Counter Pill
        Surface(
            shape = CircleShape,
            color = if (uiState.isThresholdMet) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surface
            },
            border = BorderStroke(
                width = 1.dp,
                color = if (uiState.isThresholdMet) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                }
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                if (uiState.isThresholdMet) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = when {
                        uiState.isMaxReached -> stringResource(
                            R.string.preferred_genres_requirement_max,
                            PreferredGenresUiState.MAX_GENRES
                        )

                        uiState.isThresholdMet -> stringResource(
                            R.string.preferred_genres_requirement_met,
                            uiState.selectedCount,
                            PreferredGenresUiState.MAX_GENRES
                        )

                        else -> stringResource(
                            R.string.preferred_genres_requirement_unmet,
                            PreferredGenresUiState.MIN_GENRES - uiState.selectedCount
                        )
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (uiState.isThresholdMet) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Chips Content
        when (val state = uiState.genresState) {
            is UiState.Idle,
            is UiState.Loading -> {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                ) {
                    ShowTimeLoadingIndicator(modifier = Modifier.size(32.dp))
                }
            }

            is UiState.Success -> {
                val genres = state.data
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(
                        MaterialTheme.spacing.small,
                        Alignment.Start
                    ),
                    verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .weight(1f, fill = false)
                ) {
                    genres.forEach { genre ->
                        val isSelected = genre.id in uiState.selectedGenreIds
                        val isChipEnabled = isSelected || !uiState.isMaxReached
                        FilterChip(
                            selected = isSelected,
                            onClick = { onGenreToggle(genre.id) },
                            enabled = isChipEnabled,
                            label = {
                                Text(
                                    text = genre.name,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontWeight = FontWeight.SemiBold
                                    )
                                )
                            },
                            shape = RoundedCornerShape(20.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                containerColor = MaterialTheme.colorScheme.surface,
                                labelColor = MaterialTheme.colorScheme.onSurface,
                                disabledContainerColor = MaterialTheme.colorScheme.surface.copy(
                                    alpha = 0.5f
                                ),
                                disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = isChipEnabled,
                                selected = isSelected,
                                borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f),
                                selectedBorderColor = MaterialTheme.colorScheme.primary,
                                disabledBorderColor = MaterialTheme.colorScheme.outlineVariant.copy(
                                    alpha = 0.3f
                                )
                            )
                        )
                    }
                }
            }

            is UiState.Error -> {
                Text(
                    text = stringResource(R.string.purchase_failed),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(MaterialTheme.spacing.large)
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // Action Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp),
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant
                )
            ) {
                Text(
                    text = stringResource(R.string.cancel),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold)
                )
            }

            Button(
                onClick = onSave,
                enabled = uiState.canSave && !uiState.isSaving,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1.5f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                if (uiState.isSaving) {
                    ShowTimeLoadingIndicator(modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                }
                Text(
                    text = stringResource(R.string.save_preferences),
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }
    }
}
