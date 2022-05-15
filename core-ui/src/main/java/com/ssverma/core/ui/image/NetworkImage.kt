package com.ssverma.core.ui.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.annotation.ExperimentalCoilApi
import coil.compose.ImagePainter
import coil.compose.rememberImagePainter

@OptIn(ExperimentalCoilApi::class)
@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    placeholder: @Composable () -> Unit = { DefaultImagePlaceHolder(modifier) },
    enableCrossFade: Boolean = true,
    crossFadeDurationMillis: Int = NetworkImageDefaults.CrossFadeDurationMs
) {
    val painter = rememberImagePainter(
        data = url,
        builder = {
            crossfade(enableCrossFade)
            crossfade(crossFadeDurationMillis)
        },
    )

    when (painter.state) {
        is ImagePainter.State.Success -> {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        is ImagePainter.State.Empty -> {
            Image(
                painter = painter,
                contentDescription = contentDescription,
                contentScale = contentScale,
                modifier = modifier
            )
        }
        is ImagePainter.State.Loading,
        is ImagePainter.State.Error -> {
            placeholder()
        }
    }
}

@Composable
fun DefaultImagePlaceHolder(modifier: Modifier = Modifier) {
    Spacer(
        modifier = modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colors.onSurface.copy(
                    alpha = NetworkImageDefaults.PlaceHolderAlpha
                )
            )
    )
}

object NetworkImageDefaults {
    const val CrossFadeDurationMs = 750
    const val PlaceHolderAlpha = 0.32f
}