package com.ssverma.core.ui

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.component.ShowTimeLoadingIndicator
import com.ssverma.core.ui.component.ShowTimeTopAppBar
import com.ssverma.core.ui.theme.LocalImages

@Composable
fun Screen(
    title: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier = modifier) {
        ShowTimeTopAppBar(title = title, onBackPressed = onBackPressed)
        content()
    }
}


@Composable
fun ScreenLoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        ShowTimeLoadingIndicator(modifier = Modifier.align(Alignment.Center))
    }
}

@Composable
fun ScreenErrorIndicator(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.fillMaxSize(),
    ) {
        content()
    }
}

@Composable
fun ScreenErrorIndicator(
    errorMessage: String,
    modifier: Modifier = Modifier,
    @DrawableRes errorIllusRes: Int = LocalImages.current.errorIllustrationResId,
    imageSize: Dp = 200.dp
) {
    ScreenErrorIndicator(modifier = modifier) {
        Column {
            Image(
                painter = painterResource(id = errorIllusRes),
                contentDescription = null,
                modifier = Modifier.size(imageSize)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMessage, style = MaterialTheme.typography.body1)
        }
    }
}


@Composable
fun ScreenErrorIndicator(
    errorMessage: String,
    modifier: Modifier = Modifier,
    @DrawableRes errorIllusRes: Int = LocalImages.current.errorIllustrationResId,
    onRetryClick: (() -> Unit),
    imageSize: Dp = 200.dp
) {
    ScreenErrorIndicator(modifier = modifier) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = errorIllusRes),
                contentDescription = null,
                modifier = Modifier.size(imageSize)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMessage, style = MaterialTheme.typography.body1)
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedButton(onClick = onRetryClick) {
                Text(text = stringResource(id = R.string.retry))
            }
        }
    }
}

@Composable
fun ScreenEmptyIndicator(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        content()
    }
}

@Composable
fun ScreenEmptyIndicator(
    modifier: Modifier = Modifier,
    emptyMessage: String = stringResource(id = R.string.no_data),
) {
    ScreenEmptyIndicator(modifier = modifier) {
        Text(
            text = emptyMessage,
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colors.onSurface.copy(alpha = 0.54f),
                    shape = MaterialTheme.shapes.medium.copy(CornerSize(8.dp))
                )
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}