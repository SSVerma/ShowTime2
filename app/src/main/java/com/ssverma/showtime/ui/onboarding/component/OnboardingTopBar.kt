package com.ssverma.showtime.ui.onboarding.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.onboarding.OnboardingStep

@Composable
fun OnboardingTopBar(
    currentStep: OnboardingStep,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .height(56.dp)
            .padding(horizontal = MaterialTheme.spacing.medium),
        contentAlignment = Alignment.CenterStart
    ) {
        // Back button on steps 2..4
        AnimatedVisibility(
            visible = !currentStep.isFirst,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = stringResource(id = R.string.back),
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // App logo/name on step 1
        AnimatedVisibility(
            visible = currentStep.isFirst,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Text(
                text = stringResource(id = R.string.app_name),
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(start = MaterialTheme.spacing.extraSmall)
            )
        }

        // Step indicator pinned on right
        Text(
            text = stringResource(
                id = R.string.onboarding_step_indicator,
                currentStep.stepIndex + 1,
                OnboardingStep.entries.size
            ),
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.align(Alignment.CenterEnd)
        )
    }
}
