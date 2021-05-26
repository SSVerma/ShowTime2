package com.ssverma.showtime.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.size
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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