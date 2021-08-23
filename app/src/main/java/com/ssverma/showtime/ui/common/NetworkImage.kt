package com.ssverma.showtime.ui.common

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
    placeholder: @Composable () -> Unit = { DefaultImagePlaceHolder() },
    fadeIn: Boolean = true
) {
    val painter = rememberImagePainter(
        data = url,
        builder = { crossfade(fadeIn) }
    )

    Image(
        painter = painter,
        contentDescription = contentDescription,
        contentScale = contentScale,
        modifier = modifier
    )

//    when (painter.state) {
//        is ImagePainter.State.Success -> {
//            Image(
//                painter = painter,
//                contentDescription = contentDescription,
//                contentScale = contentScale,
//                modifier = modifier
//            )
//        }
//        is ImagePainter.State.Empty,
//        is ImagePainter.State.Loading,
//        is ImagePainter.State.Error -> {
//            placeholder()
//        }
//    }
}

@Composable
fun DefaultImagePlaceHolder() {
    Spacer(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.surface.copy(alpha = 0.54f))
    )
}