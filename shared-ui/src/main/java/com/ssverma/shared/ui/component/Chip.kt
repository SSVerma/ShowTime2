package com.ssverma.shared.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.foundation.ClickThroughSurface

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ClickThroughFilterChip(
    selected: Boolean,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    elevation: Dp = 0.dp,
    shape: Shape = MaterialTheme.shapes.small.copy(CornerSize(percent = 50)),
    border: BorderStroke? = null,
    colors: SelectableChipColors = ChipDefaults.filterChipColors(
        backgroundColor = if (selected) {
            MaterialTheme.colors.primary
        } else {
            MaterialTheme.colors.surface
        }
    ),
    leadingIcon: @Composable (() -> Unit)? = null,
    selectedIcon: @Composable (() -> Unit)? = null,
    trailingIcon: @Composable (() -> Unit)? = null,
    content: @Composable RowScope.() -> Unit
) {
    val contentColor = colors.contentColor(enabled, selected)

    ClickThroughSurface(
        modifier = modifier,
        color = colors.backgroundColor(enabled, selected).value,
        shape = shape,
        contentColor = contentColor.value.copy(1.0f),
        border = border,
        elevation = elevation,
    ) {
        CompositionLocalProvider(LocalContentAlpha provides contentColor.value.alpha) {
            ProvideTextStyle(
                value = MaterialTheme.typography.body2
            ) {
                Row(
                    Modifier
                        .defaultMinSize(
                            minHeight = ChipDefaults.MinHeight
                        )
                        .padding(
                            start =
                            if (leadingIcon != null || (selected && selectedIcon != null)) {
                                0.dp
                            } else {
                                HorizontalPadding
                            },
                            end =
                            if (trailingIcon == null) {
                                HorizontalPadding
                            } else {
                                0.dp
                            }
                        ),
                    horizontalArrangement = Arrangement.Start,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (leadingIcon != null || (selected && selectedIcon != null)) {
                        Spacer(Modifier.width(LeadingIconStartSpacing))
                        Box {
                            if (leadingIcon != null) {
                                val leadingIconColor = colors.leadingIconColor(
                                    enabled,
                                    selected
                                )
                                CompositionLocalProvider(
                                    LocalContentColor provides leadingIconColor.value,
                                    LocalContentAlpha provides leadingIconColor.value.alpha,
                                    content = leadingIcon
                                )
                            }
                            if (selected && selectedIcon != null) {
                                var overlayModifier: Modifier = Modifier
                                var iconColor = contentColor.value
                                if (leadingIcon != null) {
                                    overlayModifier = Modifier
                                        .requiredSize(SelectedIconContainerSize)
                                        .background(
                                            color = contentColor.value,
                                            shape = CircleShape
                                        )
                                        .clip(CircleShape)

                                    iconColor = colors.backgroundColor(enabled, selected).value
                                }
                                Box(
                                    modifier = overlayModifier,
                                    contentAlignment = Alignment.Center
                                ) {
                                    CompositionLocalProvider(
                                        LocalContentColor provides iconColor,
                                        content = selectedIcon
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.width(LeadingIconEndSpacing))
                    }
                    content()
                    if (trailingIcon != null) {
                        Spacer(Modifier.width(TrailingIconSpacing))
                        trailingIcon()
                        Spacer(Modifier.width(TrailingIconSpacing))
                    }
                }
            }
        }
    }
}

private val HorizontalPadding = 12.dp
private val LeadingIconStartSpacing = 4.dp
private val LeadingIconEndSpacing = 8.dp
private val TrailingIconSpacing = 8.dp
private val SelectedIconContainerSize = 24.dp
