package com.ssverma.showtime.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import com.google.accompanist.coil.rememberCoilPainter
import com.google.accompanist.imageloading.ImageLoadState

@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    placeholder: @Composable () -> Unit = { DefaultImagePlaceHolder() },
    fadeIn: Boolean = true
) {
    val painter = rememberCoilPainter(
        request = url,
        fadeIn = fadeIn
    )

    when (painter.loadState) {
        is ImageLoadState.Success -> {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        is ImageLoadState.Empty,
        is ImageLoadState.Loading,
        is ImageLoadState.Error -> {
            placeholder()
        }
    }
}

@Composable
fun DefaultImagePlaceHolder() {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface.copy(alpha = 0.54f))
    )
}