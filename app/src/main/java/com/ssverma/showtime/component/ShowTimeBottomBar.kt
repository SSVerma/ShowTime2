package com.ssverma.showtime.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
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
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItem
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItems

fun isHomePage(
    currentNavKey: NavKey?,
    bottomNavDestinations: List<ShowTimeTopLevelNavItem>
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
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val activeContentColor = MaterialTheme.colorScheme.primary
    val inactiveContentColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)

    val animatedColor by animateColorAsState(
        targetValue = if (selected) activeContentColor else inactiveContentColor,
        animationSpec = tween(durationMillis = 200),
        label = "TabColor"
    )

    val capsuleColor by animateColorAsState(
        targetValue = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
        animationSpec = tween(durationMillis = 200),
        label = "CapsuleColor"
    )

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .clip(CircleShape)
            .background(capsuleColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = ripple(bounded = true)
            ) {
                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                onClick()
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = stringResource(id = titleResId),
                tint = animatedColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = stringResource(id = titleResId),
                color = animatedColor,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun ShowTimeBottomBar(
    currentNavKey: NavKey?,
    topLevelNavKey: NavKey,
    onTopLevelNavItemSelected: (ShowTimeTopLevelNavItem) -> Unit,
    modifier: Modifier = Modifier,
    bottomNavItems: List<ShowTimeTopLevelNavItem> = ShowTimeTopLevelNavItems,
) {
    if (!isHomePage(currentNavKey, bottomNavItems)) {
        return
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            .shadow(
                elevation = 5.dp,
                shape = CircleShape,
                ambientColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.10f),
                spotColor = MaterialTheme.colorScheme.scrim.copy(alpha = 0.18f),
                clip = false
            ),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp,
            border = BorderStroke(
                width = 1.dp,
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                bottomNavItems.forEach { item ->
                    ShowTimeToolbarTab(
                        iconResId = item.iconResId,
                        titleResId = item.titleResId,
                        selected = item.navKey == topLevelNavKey || item.navKey::class == topLevelNavKey::class,
                        onClick = { onTopLevelNavItemSelected(item) },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
            }
        }
    }
}
