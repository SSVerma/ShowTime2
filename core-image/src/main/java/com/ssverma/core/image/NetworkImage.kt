package com.ssverma.core.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import coil.compose.SubcomposeAsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest

object NetworkImageDefaults {
    const val CrossFadeDurationMs = 650
    const val PlaceHolderAlpha = 0.1f
}

@Composable
fun NetworkImage(
    url: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit, // Safe global default
    loadingPlaceholder: @Composable () -> Unit = { DefaultImagePlaceHolder() },
    errorPlaceholder: @Composable () -> Unit = { ErrorImagePlaceHolder() },
    enableCrossFade: Boolean = true,
    crossFadeDurationMillis: Int = NetworkImageDefaults.CrossFadeDurationMs,
    diskCachePolicy: CachePolicy = CachePolicy.ENABLED
) {
    SubcomposeAsyncImage(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(enableCrossFade)
            .crossfade(crossFadeDurationMillis)
            .diskCachePolicy(diskCachePolicy)
            .build(),
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier,
        loading = { loadingPlaceholder() },
        error = { errorPlaceholder() }
    )
}

@Composable
fun DefaultImagePlaceHolder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.onSurface.copy(
                    alpha = NetworkImageDefaults.PlaceHolderAlpha
                )
            )
    )
}

@Composable
fun ErrorImagePlaceHolder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = NetworkImageDefaults.PlaceHolderAlpha
                )
            )
    )
}
