package com.ssverma.showtime.ui.onboarding.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.onboarding.OnboardingStep
import com.ssverma.showtime.ui.onboarding.OnboardingUiState

@Composable
fun OnboardingBottomBar(
    uiState: OnboardingUiState,
    onNextStep: () -> Unit,
    onSkipStep: () -> Unit,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        shadowElevation = 8.dp,
        border = BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(
                    horizontal = MaterialTheme.spacing.medium,
                    vertical = MaterialTheme.spacing.small
                )
        ) {
            // Step Dots
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
            ) {
                OnboardingStep.entries.forEach { step ->
                    val isCurrent = step == uiState.currentStep
                    val isPast = step.stepIndex < uiState.currentStep.stepIndex

                    val dotWidth by animateDpAsState(
                        targetValue = if (isCurrent) 24.dp else 8.dp,
                        animationSpec = tween(300),
                        label = "dot_width"
                    )

                    val dotColor by animateColorAsState(
                        targetValue = when {
                            isCurrent -> MaterialTheme.colorScheme.primary
                            isPast -> MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                            else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                        },
                        animationSpec = tween(300),
                        label = "dot_color"
                    )

                    Box(
                        modifier = Modifier
                            .height(8.dp)
                            .width(dotWidth)
                            .clip(CircleShape)
                            .background(dotColor)
                    )
                }
            }

            val isPrimaryEnabled = when (uiState.currentStep) {
                OnboardingStep.Welcome -> true
                OnboardingStep.Subscriptions -> uiState.selectedProviderIds.isNotEmpty()
                OnboardingStep.Taste -> uiState.canProceedFromTaste
                OnboardingStep.Cloud -> true
            }

            val primaryText = when (uiState.currentStep) {
                OnboardingStep.Welcome -> stringResource(id = R.string.onboarding_welcome_cta)
                OnboardingStep.Subscriptions -> {
                    if (uiState.selectedProviderIds.isNotEmpty()) {
                        stringResource(
                            id = R.string.onboarding_subs_continue_count,
                            uiState.selectedProviderIds.size
                        )
                    } else {
                        stringResource(id = R.string.onboarding_continue)
                    }
                }

                OnboardingStep.Taste -> {
                    if (uiState.canProceedFromTaste) {
                        stringResource(
                            id = R.string.onboarding_taste_continue_count,
                            uiState.selectedGenreIds.size
                        )
                    } else {
                        stringResource(
                            id = R.string.onboarding_taste_requirement_unmet,
                            3 - uiState.selectedGenreIds.size
                        )
                    }
                }

                OnboardingStep.Cloud -> stringResource(id = R.string.onboarding_complete_cta)
            }

            if (uiState.currentStep.isFirst || uiState.currentStep.isLast) {
                Button(
                    onClick = {
                        if (uiState.currentStep.isLast) {
                            onComplete()
                        } else {
                            onNextStep()
                        }
                    },
                    enabled = isPrimaryEnabled,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = primaryText,
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = if (uiState.currentStep.isLast) Icons.Rounded.Check else Icons.AutoMirrored.Rounded.ArrowForward,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.medium),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedButton(
                        onClick = onSkipStep,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.onboarding_skip),
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = onNextStep,
                        enabled = isPrimaryEnabled,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier
                            .weight(2f)
                            .height(50.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = primaryText,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
