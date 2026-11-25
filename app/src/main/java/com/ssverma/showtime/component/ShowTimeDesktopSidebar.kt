package com.ssverma.showtime.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation3.runtime.NavKey
import com.ssverma.core.ui.component.ShowTimeLogo
import com.ssverma.core.ui.layout.windowTopBarInsetsPadding
import com.ssverma.core.ui.theme.spacing
import com.ssverma.showtime.BuildConfig
import com.ssverma.showtime.R
import com.ssverma.showtime.navigation.ShowTimeRailNavItems
import com.ssverma.showtime.navigation.ShowTimeTopLevelNavItem

/**
 * Permanent 264dp navigation sidebar displayed on Large desktop and Googlebook viewports.
 */
@Composable
fun ShowTimeDesktopSidebar(
    currentNavKey: NavKey?,
    topLevelNavKey: NavKey,
    onTopLevelNavItemSelected: (ShowTimeTopLevelNavItem) -> Unit,
    menuActions: ShowTimeMenuActions,
    modifier: Modifier = Modifier
) {
    val sections = remember(menuActions) {
        buildShowTimeMenuSections(
            actions = menuActions,
            includePeopleItem = false,
            includeKeyboardShortcutsItem = true
        )
    }
    val hasMatchingSecondaryItem = sections.any { section ->
        section.items.any { item ->
            item.targetKeyClass != null &&
                    currentNavKey != null &&
                    currentNavKey::class == item.targetKeyClass
        }
    }

    Row(
        modifier = modifier
            .fillMaxHeight()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .width(DesktopSidebarDefaults.Width)
                .fillMaxHeight()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .windowTopBarInsetsPadding()
                    .navigationBarsPadding()
                    .padding(bottom = MaterialTheme.spacing.large)
            ) {
                Box(
                    contentAlignment = Alignment.CenterStart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(DesktopSidebarDefaults.HeaderHeight)
                        .padding(horizontal = MaterialTheme.spacing.smallMedium)
                ) {
                    Surface(
                        onClick = menuActions.onReplaySplash,
                        shape = RoundedCornerShape(DesktopSidebarDefaults.BrandCornerRadius),
                        color = Color.Transparent,
                        modifier = Modifier
                            .fillMaxWidth()
                            .pointerHoverIcon(PointerIcon.Hand)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(
                                horizontal = MaterialTheme.spacing.small,
                                vertical = MaterialTheme.spacing.extraSmall
                            )
                        ) {
                            ShowTimeLogo(modifier = Modifier.size(DesktopSidebarDefaults.LogoSize))

                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.smallMedium))

                            Column {
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "v${BuildConfig.VERSION_NAME}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                )

                Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

                ShowTimeRailNavItems.forEach { navItem ->
                    val isSelected = !hasMatchingSecondaryItem && (
                            navItem.navKey == topLevelNavKey ||
                                    navItem.navKey::class == topLevelNavKey::class
                            )

                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = stringResource(id = navItem.titleResId),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = navItem.iconResId),
                                contentDescription = stringResource(id = navItem.titleResId),
                                modifier = Modifier.size(DesktopSidebarDefaults.IconSize)
                            )
                        },
                        selected = isSelected,
                        onClick = { onTopLevelNavItemSelected(navItem) },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.16f),
                            selectedIconColor = MaterialTheme.colorScheme.primary,
                            selectedTextColor = MaterialTheme.colorScheme.primary,
                            unselectedContainerColor = Color.Transparent,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurface
                        ),
                        shape = RoundedCornerShape(DesktopSidebarDefaults.ItemCornerRadius),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                horizontal = MaterialTheme.spacing.smallMedium,
                                vertical = 2.dp
                            )
                            .height(DesktopSidebarDefaults.ItemHeight)
                            .pointerHoverIcon(PointerIcon.Hand)
                    )
                }

                sections.forEach { section ->
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.medium,
                            vertical = MaterialTheme.spacing.small
                        ),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)
                    )

                    Text(
                        text = stringResource(id = section.titleResId),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(
                            horizontal = MaterialTheme.spacing.large,
                            vertical = MaterialTheme.spacing.small
                        )
                    )

                    section.items.forEach { item ->
                        val isItemSelected = item.targetKeyClass != null &&
                                currentNavKey != null &&
                                currentNavKey::class == item.targetKeyClass

                        val iconTint = if (isItemSelected || item.usePrimaryTint) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        NavigationDrawerItem(
                            label = {
                                Text(
                                    text = stringResource(id = item.labelResId),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isItemSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            icon = {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = stringResource(id = item.labelResId),
                                    tint = iconTint,
                                    modifier = Modifier.size(DesktopSidebarDefaults.IconSize)
                                )
                            },
                            selected = isItemSelected,
                            onClick = item.onClick,
                            colors = NavigationDrawerItemDefaults.colors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(
                                    alpha = 0.16f
                                ),
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedContainerColor = Color.Transparent,
                                unselectedIconColor = iconTint,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurface
                            ),
                            shape = RoundedCornerShape(DesktopSidebarDefaults.ItemCornerRadius),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(
                                    horizontal = MaterialTheme.spacing.smallMedium,
                                    vertical = 2.dp
                                )
                                .height(DesktopSidebarDefaults.ItemHeight)
                                .pointerHoverIcon(PointerIcon.Hand)
                        )
                    }
                }
            }
        }

        VerticalDivider(
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    }
}

private object DesktopSidebarDefaults {
    val Width = 248.dp
    val HeaderHeight = 64.dp
    val ItemHeight = 44.dp
    val BrandCornerRadius = 14.dp
    val ItemCornerRadius = 12.dp
    val IconSize = 20.dp
    val LogoSize = 36.dp
}
