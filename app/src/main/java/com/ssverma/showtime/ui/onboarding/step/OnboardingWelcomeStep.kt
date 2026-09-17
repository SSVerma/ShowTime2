package com.ssverma.showtime.ui.onboarding.step

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.rounded.Bookmarks
import androidx.compose.material.icons.rounded.MovieFilter
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.onboarding.illustration.CinemaWelcomeArt

@Composable
fun OnboardingWelcomeStep(
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MaterialTheme.spacing.large)
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
                text = stringResource(id = R.string.onboarding_welcome_badge),
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                ),
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Cinema visual
        CinemaWelcomeArt(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

        // Headline & Subtitle
        Text(
            text = stringResource(id = R.string.onboarding_welcome_title),
            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        Text(
            text = stringResource(id = R.string.onboarding_welcome_tagline),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = MaterialTheme.spacing.small)
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // 3 Feature Highlight Rows
        WelcomeFeaturePill(
            icon = Icons.Rounded.MovieFilter,
            title = stringResource(id = R.string.onboarding_welcome_perk_discovery_title),
            description = stringResource(id = R.string.onboarding_welcome_perk_discovery_desc),
            iconContainerColor = MaterialTheme.colorScheme.primaryContainer,
            iconTint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        WelcomeFeaturePill(
            icon = Icons.Rounded.Bookmarks,
            title = stringResource(id = R.string.onboarding_welcome_perk_diary_title),
            description = stringResource(id = R.string.onboarding_welcome_perk_diary_desc),
            iconContainerColor = MaterialTheme.colorScheme.tertiaryContainer,
            iconTint = MaterialTheme.colorScheme.tertiary
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        WelcomeFeaturePill(
            icon = Icons.Rounded.Shield,
            title = stringResource(id = R.string.onboarding_welcome_perk_privacy_title),
            description = stringResource(id = R.string.onboarding_welcome_perk_privacy_desc),
            iconContainerColor = MaterialTheme.colorScheme.secondaryContainer,
            iconTint = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))
    }
}

@Composable
private fun WelcomeFeaturePill(
    icon: ImageVector,
    title: String,
    description: String,
    iconContainerColor: androidx.compose.ui.graphics.Color,
    iconTint: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.8f)
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(
                horizontal = MaterialTheme.spacing.medium,
                vertical = MaterialTheme.spacing.medium
            )
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(iconContainerColor)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(MaterialTheme.spacing.medium))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
