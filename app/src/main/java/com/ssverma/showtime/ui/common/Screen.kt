package com.ssverma.showtime.ui.common

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.theme.LocalImages

@Composable
fun Screen(content: @Composable () -> Unit) {
    Surface {
        content()
    }
}

@Composable
fun Screen(
    title: String,
    onBackPressed: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Surface(modifier = modifier) {
        Column {
            AppTopAppBar(title = title, onBackPressed = onBackPressed)
            content()
        }
    }
}


@Composable
fun ScreenLoadingIndicator(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        AppLoadingIndicator(modifier = Modifier.align(Alignment.Center))
    }
}


@Composable
fun ScreenErrorIndicator(
    errorMessage: String,
    modifier: Modifier = Modifier,
    @DrawableRes errorIllusRes: Int = LocalImages.current.errorIllustration,
    onRetryClicked: (() -> Unit)? = null,
    imageSize: Dp = 200.dp
) {
    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = errorIllusRes),
                contentDescription = null,
                modifier = Modifier.size(imageSize)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = errorMessage, style = MaterialTheme.typography.body1)
            onRetryClicked?.let {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(onClick = it) {
                    Text(text = stringResource(id = R.string.retry))
                }
            }
        }
    }
}