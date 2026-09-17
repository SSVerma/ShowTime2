package com.ssverma.showtime.ui.onboarding.step

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.Genre
import com.ssverma.showtime.R

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun OnboardingTasteStep(
    genres: List<Genre>,
    selectedGenreIds: Set<Int>,
    isLoading: Boolean,
    onToggleGenre: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedCount = selectedGenreIds.size
    val isThresholdMet = selectedCount >= 3
    val isMaxReached = selectedCount >= 5

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.medium)
    ) {
        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Badge
        Box(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                    shape = CircleShape
                )
                .padding(horizontal = 16.dp, vertical = 7.dp)
        ) {
            Text(
                text = stringResource(id = R.string.onboarding_taste_badge),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        Text(
            text = stringResource(id = R.string.onboarding_taste_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Text(
            text = stringResource(id = R.string.onboarding_taste_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.medium)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Counter status pill
        Surface(
            shape = CircleShape,
            color = if (isThresholdMet) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            } else {
                MaterialTheme.colorScheme.surface
            },
            border = BorderStroke(
                width = 1.dp,
                color = if (isThresholdMet) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                } else {
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
                }
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                if (isThresholdMet) {
                    Icon(
                        imageVector = Icons.Rounded.Check,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                Text(
                    text = when {
                        isMaxReached -> stringResource(
                            id = R.string.onboarding_taste_requirement_max,
                            5
                        )

                        isThresholdMet -> stringResource(
                            id = R.string.onboarding_taste_requirement_met,
                            selectedCount,
                            5
                        )

                        else -> stringResource(
                            id = R.string.onboarding_taste_requirement_unmet,
                            3 - selectedCount
                        )
                    },
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = if (isThresholdMet) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        if (isLoading && genres.isEmpty()) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
            ) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(36.dp)
                )
            }
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(
                    MaterialTheme.spacing.small,
                    Alignment.CenterHorizontally
                ),
                verticalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
                modifier = Modifier.fillMaxWidth()
            ) {
                genres.forEach { genre ->
                    val isSelected = genre.id in selectedGenreIds
                    val isChipEnabled = isSelected || !isMaxReached
                    FilterChip(
                        selected = isSelected,
                        onClick = { onToggleGenre(genre.id) },
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
                            disabledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
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

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
    }
}
