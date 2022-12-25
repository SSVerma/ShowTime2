package com.ssverma.core.image

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.CachePolicy
import coil.request.ImageRequest

@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit,
    loadingPlaceholder: @Composable () -> Unit = { DefaultImagePlaceHolder(modifier) },
    errorPlaceholder: @Composable () -> Unit = { DefaultImagePlaceHolder(modifier) },
    enableCrossFade: Boolean = true,
    crossFadeDurationMillis: Int = NetworkImageDefaults.CrossFadeDurationMs,
    diskCachePolicy: CachePolicy = CachePolicy.ENABLED
) {
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(enableCrossFade)
            .diskCachePolicy(diskCachePolicy)
            .crossfade(crossFadeDurationMillis)
            .build()
    )

    // Modifier is shared by the parent box, image and placeholder because all three should be
    // in same dimensions.
    Box(modifier = modifier) {
        Image(
            painter = painter,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = modifier
        )

        when (painter.state) {
            is AsyncImagePainter.State.Success -> {
                // No op, can use it to perform transitions
            }
            is AsyncImagePainter.State.Empty -> {
                //No op
            }
            is AsyncImagePainter.State.Loading -> {
                loadingPlaceholder()
            }
            is AsyncImagePainter.State.Error -> {
                errorPlaceholder()
            }
        }
    }
}

@Composable
fun DefaultImagePlaceHolder(modifier: Modifier = Modifier) {
    Box(
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
    const val CrossFadeDurationMs = 650
    const val PlaceHolderAlpha = 0.32f
}