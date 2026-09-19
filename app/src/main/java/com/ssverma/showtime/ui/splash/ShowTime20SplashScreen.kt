package com.ssverma.showtime.ui.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.splash.illustration.ShowTime20MilestoneArt
import kotlinx.coroutines.launch

/**
 * Celebratory splash screen revealing the "ShowTime: 2.0" brand milestone.
 *
 * Choreography:
 * 1. (0ms - 450ms): Atmospheric cinema art & rotating film reel tracks scale in.
 * 2. (450ms - 950ms): Central illuminated "2.0" hero emblem pops with spring physics & haptic feedback.
 * 3. (950ms - 1450ms): "SHOWTIME" wordmark & "VERSION 2.0 PREMIERE" label glide in with light shimmer.
 * 4. (1300ms - 1650ms): Tagline fades in softly.
 * 5. (1600ms - 2000ms): "Let's Go" CTA button reveals with spring scale.
 * 6. User can take their time to admire the art and tap "Let's Go" (or screen) to continue smoothly.
 */
@Composable
fun ShowTime20SplashScreen(
    onSplashComplete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    val artScale = remember { Animatable(0.7f) }
    val artAlpha = remember { Animatable(0f) }

    val badgeScale = remember { Animatable(0f) }
    val badgeAlpha = remember { Animatable(0f) }

    val shimmerSweep = remember { Animatable(0f) }

    val titleAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }
    val buttonAlpha = remember { Animatable(0f) }
    val buttonScale = remember { Animatable(0.8f) }

    val exitAlpha = remember { Animatable(1f) }
    val exitScale = remember { Animatable(1f) }

    var isExiting by remember { mutableStateOf(false) }

    val completeAction: () -> Unit = {
        if (!isExiting) {
            isExiting = true
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            coroutineScope.launch {
                launch {
                    exitScale.animateTo(
                        targetValue = 1.05f,
                        animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                    )
                }
                exitAlpha.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 200, easing = FastOutSlowInEasing)
                )
                onSplashComplete()
            }
        }
    }

    LaunchedEffect(Unit) {
        // Stage 1: Cinema Art & Film Reel Entrance (0ms - 450ms)
        launch {
            artAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 400, easing = FastOutSlowInEasing)
            )
        }
        artScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        // Stage 2: Central "2.0" Emblem Pop + Haptic (450ms - 950ms)
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        launch {
            badgeAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 250)
            )
        }
        badgeScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        )

        // Stage 3: Title & Shimmer Sweep (950ms - 1450ms)
        launch {
            titleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }
        shimmerSweep.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 500, easing = LinearEasing)
        )

        // Stage 4: Subtitle & Tagline (1300ms - 1650ms)
        launch {
            subtitleAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }

        // Stage 5: "Let's Go" CTA Button Reveal (1600ms - 2000ms)
        launch {
            buttonAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing)
            )
        }
        buttonScale.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                completeAction()
            }
            .scale(exitScale.value)
            .alpha(exitAlpha.value)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .padding(horizontal = MaterialTheme.spacing.large)
                .statusBarsPadding()
        ) {
            // Milestone Vector Illustration
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .scale(artScale.value)
                    .alpha(artAlpha.value)
            ) {
                ShowTime20MilestoneArt(
                    badgeScale = badgeScale.value,
                    badgeAlpha = badgeAlpha.value,
                    shimmerSweep = shimmerSweep.value
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

            // Brand Typography
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.alpha(titleAlpha.value)
            ) {
                Text(
                    text = stringResource(id = R.string.splash_version_label),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 3.sp
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraSmall))

                Text(
                    text = stringResource(id = R.string.app_name),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onBackground,
                    letterSpacing = (-1).sp
                )
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

            // Tagline
            Text(
                text = stringResource(id = R.string.splash_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.extraLarge))

            // "Let's Go" CTA Button
            Button(
                onClick = { completeAction() },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ),
                contentPadding = PaddingValues(horizontal = 32.dp, vertical = 14.dp),
                modifier = Modifier
                    .scale(buttonScale.value)
                    .alpha(buttonAlpha.value)
            ) {
                Text(
                    text = stringResource(id = R.string.splash_lets_go),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowForward,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Bottom Tap to Skip / Continue Hint
        Text(
            text = stringResource(id = R.string.splash_tap_to_skip),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = MaterialTheme.spacing.large)
                .alpha(subtitleAlpha.value * 0.7f)
        )
    }
}
