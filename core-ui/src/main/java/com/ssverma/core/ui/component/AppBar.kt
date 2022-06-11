package com.ssverma.core.ui.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import com.ssverma.core.ui.R

@Composable
fun ShowTimeTopAppBar(
    title: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    navIcon: ImageVector = Icons.Default.ArrowBack,
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
                    painter = rememberVectorPainter(image = navIcon),
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