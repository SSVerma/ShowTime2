package com.ssverma.showtime.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.ssverma.common.ui.R as CommonUiR
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItem
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItems

fun isHomePage(
    currentNavKey: NavKey?,
    bottomNavDestinations: List<ShowTimeTopLevelNavItem>,
): Boolean {
    return bottomNavDestinations.any {
        it.navKey == currentNavKey || (currentNavKey != null && it.navKey::class == currentNavKey::class)
    }
}

@Composable
fun ShowTimeToolbarTab(
    iconResId: Int,
    titleResId: Int,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val haptic = LocalHapticFeedback.current
    val activeContentColor = MaterialTheme.colorScheme.primary
    val inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)

    val animatedColor by animateColorAsState(
        targetValue = if (selected) activeContentColor else inactiveContentColor,
        animationSpec = tween(durationMillis = 220),
        label = "TabColor",
    )

    val capsuleColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = tween(durationMillis = 220),
        label = "CapsuleColor",
    )

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .clip(CircleShape)
            .background(capsuleColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true),
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 6.dp),
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = stringResource(id = titleResId),
                tint = animatedColor,
                modifier = Modifier.size(20.dp),
            )
            AnimatedVisibility(
                visible = selected,
                enter = fadeIn(animationSpec = tween(180)) + expandHorizontally(
                    animationSpec = tween(220, easing = FastOutSlowInEasing),
                    expandFrom = Alignment.Start,
                ),
                exit = fadeOut(animationSpec = tween(140)) + shrinkHorizontally(
                    animationSpec = tween(180, easing = FastOutSlowInEasing),
                    shrinkTowards = Alignment.Start,
                ),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = stringResource(id = titleResId),
                        color = animatedColor,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false,
                    )
                }
            }
        }
    }
}

@Composable
fun ShowTimeBottomBar(
    currentNavKey: NavKey?,
    topLevelNavKey: NavKey,
    onTopLevelNavItemSelected: (ShowTimeTopLevelNavItem) -> Unit,
    modifier: Modifier = Modifier,
    onAiAssistantClick: () -> Unit = {},
    isAiAvailable: Boolean = false,
    bottomNavItems: List<ShowTimeTopLevelNavItem> = ShowTimeTopLevelNavItems,
) {
    if (!isHomePage(currentNavKey, bottomNavItems)) {
        return
    }

    val haptic = LocalHapticFeedback.current

    Row(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // Navigation Pill
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
            ),
            modifier = Modifier
                .then(
                    if (isAiAvailable) {
                        Modifier.weight(1f)
                    } else {
                        Modifier.fillMaxWidth()
                    }
                )
                .height(56.dp)
                .shadow(
                    elevation = 5.dp,
                    shape = CircleShape,
                    ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.10f),
                    spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f),
                    clip = false,
                ),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                bottomNavItems.forEach { item ->
                    val isSelected =
                        item.navKey == topLevelNavKey || item.navKey::class == topLevelNavKey::class
                    val animatedWeight by animateFloatAsState(
                        targetValue = if (isSelected) 1.6f else 1f,
                        animationSpec = tween(durationMillis = 220, easing = FastOutSlowInEasing),
                        label = "TabWeight",
                    )
                    ShowTimeToolbarTab(
                        iconResId = item.iconResId,
                        titleResId = item.titleResId,
                        selected = isSelected,
                        onClick = { onTopLevelNavItemSelected(item) },
                        modifier = Modifier
                            .weight(animatedWeight)
                            .fillMaxHeight(),
                    )
                }
            }
        }

        // Circular AI Search Button (Google Photos floating style)
        if (isAiAvailable) {
            Spacer(modifier = Modifier.width(10.dp))

            Surface(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    onAiAssistantClick()
                },
                shape = CircleShape,
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 0.dp,
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f),
                ),
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = 5.dp,
                        shape = CircleShape,
                        ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.10f),
                        spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f),
                        clip = false,
                    ),
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = stringResource(id = CommonUiR.string.cinephile_ai_title),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}
