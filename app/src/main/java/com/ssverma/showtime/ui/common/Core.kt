package com.ssverma.showtime.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssverma.showtime.R

val AppIcons = Icons.Default

fun Modifier.scrim(
    colors: List<Color>
): Modifier = drawWithContent {
    drawContent()
    drawRect(Brush.verticalGradient(colors))
}

@Composable
fun ClickThroughSurface(
    modifier: Modifier = Modifier,
    shape: Shape = RectangleShape,
    color: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(color),
    border: BorderStroke? = null,
    elevation: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val elevationOverlay = LocalElevationOverlay.current
    val absoluteElevation = LocalAbsoluteElevation.current + elevation
    val backgroundColor =
        if (color == MaterialTheme.colors.surface && elevationOverlay != null) {
            elevationOverlay.apply(color, absoluteElevation)
        } else {
            color
        }
    CompositionLocalProvider(
        LocalContentColor provides contentColor,
        LocalAbsoluteElevation provides absoluteElevation
    ) {
        Box(
            modifier
                .shadow(elevation, shape, clip = false)
                .then(if (border != null) Modifier.border(border, shape) else Modifier)
                .background(
                    color = backgroundColor,
                    shape = shape
                )
                .clip(shape),
            propagateMinConstraints = true
        ) {
            content()
        }
    }
}

@Composable
fun AppLoadingIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(modifier = modifier.size(24.dp), strokeWidth = 2.dp)
}

@Composable
fun AppTopAppBar(
    title: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    @DrawableRes navIconRes: Int = R.drawable.ic_arrow_back,
    actions: @Composable RowScope.() -> Unit = {},
    backgroundColor: Color = MaterialTheme.colors.surface,
    contentColor: Color = contentColorFor(backgroundColor = backgroundColor),
    elevation: Dp = AppBarDefaults.TopAppBarElevation
) {
    TopAppBar(
        title = { Text(text = title) },
        contentColor = contentColor,
        navigationIcon = {
            IconButton(onClick = onBackPressed) {
                Icon(
                    painter = painterResource(id = navIconRes),
                    contentDescription = stringResource(R.string.back)
                )
            }
        },
        backgroundColor = backgroundColor,
        actions = actions,
        elevation = elevation,
        modifier = modifier
    )
}