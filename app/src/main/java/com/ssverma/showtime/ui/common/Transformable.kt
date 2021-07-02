package com.ssverma.showtime.ui.common

import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer

@Composable
fun Transformable(
    content: @Composable () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }
    var rotation by remember { mutableStateOf(0f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    val transformableState =
        rememberTransformableState { zoomChange, offsetChange, rotationChange ->
            scale *= zoomChange
            rotation += rotationChange
            offset += offsetChange
        }

    Box(
        modifier = Modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale,
                rotationZ = rotation,
                translationX = offset.x,
                translationY = offset.y
            )
            .transformable(state = transformableState)
    ) {
        content()
    }
}