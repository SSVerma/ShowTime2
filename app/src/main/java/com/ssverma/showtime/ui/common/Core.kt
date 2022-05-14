package com.ssverma.showtime.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
    backgroundColor: Color = MaterialTheme.colors.background,
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

@Composable
fun Emphasize(
    contentAlpha: Float = ContentAlpha.medium,
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalContentAlpha provides contentAlpha) {
        content()
    }
}

@Composable
fun Avatar(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .background(color = MaterialTheme.colors.surface, shape = CircleShape)
            .size(48.dp)
            .border(
                width = 2.dp,
                color = MaterialTheme.colors.primaryVariant,
                shape = CircleShape
            )
            .padding(4.dp)
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        NetworkImage(
            url = imageUrl,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
}

@Composable
fun BackdropNavigationAction(
    onIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = AppIcons.ArrowBack
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colors.surface.copy(alpha = 0.54f),
        modifier = modifier
            .statusBarsPadding()
            .padding(start = 16.dp, top = 8.dp)
            .size(40.dp)
    ) {
        IconButton(onClick = onIconClick) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colors.onSurface
            )
        }
    }
}