package com.ssverma.core.ui.component

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowTimeTopAppBar(
    title: @Composable () -> Unit,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    navIcon: ImageVector = Icons.AutoMirrored.Rounded.ArrowBack,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    showBottomShadow: Boolean = true,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        scrolledContainerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
) {
    val isScrolled = if (showBottomShadow) {
        scrollBehavior?.state?.let {
            it.contentOffset < -1f || it.overlappedFraction > 0.01f
        } ?: false
    } else false

    val glowAlpha by animateFloatAsState(
        targetValue = if (isScrolled) 1f else 0f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "TopBarGlowAlpha"
    )

    val elevation by animateDpAsState(
        targetValue = if (isScrolled) 3.dp else 0.dp,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "TopBarElevation"
    )

    val primaryColor = MaterialTheme.colorScheme.primary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                if (glowAlpha > 0f) {
                    val glowHeight = 6.dp.toPx()
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                primaryColor.copy(alpha = 0.12f * glowAlpha),
                                primaryColor.copy(alpha = 0.04f * glowAlpha),
                                Color.Transparent
                            ),
                            startY = size.height,
                            endY = size.height + glowHeight
                        ),
                        topLeft = Offset(0f, size.height),
                        size = Size(size.width, glowHeight)
                    )
                }
            }
            .shadow(
                elevation = elevation,
                ambientColor = primaryColor.copy(alpha = 0.12f),
                spotColor = primaryColor.copy(alpha = 0.25f),
                clip = false
            )
    ) {
        CenterAlignedTopAppBar(
            title = title,
            navigationIcon = {
                IconButton(onClick = onBackPressed) {
                    Icon(
                        painter = rememberVectorPainter(image = navIcon),
                        contentDescription = stringResource(id = R.string.back)
                    )
                }
            },
            actions = actions,
            colors = colors,
            scrollBehavior = scrollBehavior
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShowTimeTopAppBar(
    title: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    navIcon: ImageVector = Icons.AutoMirrored.Rounded.ArrowBack,
    actions: @Composable RowScope.() -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.background,
        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
) {
    ShowTimeTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        onBackPressed = onBackPressed,
        modifier = modifier,
        navIcon = navIcon,
        actions = actions,
        scrollBehavior = scrollBehavior,
        colors = colors
    )
}
