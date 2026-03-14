package com.ssverma.shared.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.shared.domain.model.ImageShot

@Composable
fun ImageShotItem(
    imageShot: ImageShot,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.background,
    contentScale: ContentScale = ContentScale.Crop,
    shape: Shape = MaterialTheme.shapes.medium.copy(CornerSize(16.dp))
) {
    if (onClick != null) {
        Card(
            shape = shape,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            onClick = onClick,
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            ImageContent(imageShot, contentScale)
        }
    } else {
        Card(
            shape = shape,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = backgroundColor)
        ) {
            ImageContent(imageShot, contentScale)
        }
    }
}

@Composable
private fun ImageContent(imageShot: ImageShot, contentScale: ContentScale) {
    NetworkImage(
        url = imageShot.imageUrl,
        contentDescription = null,
        contentScale = contentScale,
        modifier = Modifier.fillMaxSize()
    )
}
