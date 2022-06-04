package com.ssverma.shared.ui.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.Card
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.ssverma.shared.domain.model.ImageShot
import com.ssverma.core.ui.image.NetworkImage

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ImageShotItem(
    imageShot: ImageShot,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = MaterialTheme.colors.background,
    contentScale: ContentScale = ContentScale.Fit,
    shape: Shape = MaterialTheme.shapes.medium.copy(CornerSize(16.dp))
) {
    Card(
        shape = shape,
        elevation = 0.dp,
        onClick = onClick,
        modifier = modifier,
        backgroundColor = backgroundColor
    ) {
        NetworkImage(
            url = imageShot.imageUrl,
            contentDescription = null,
            contentScale = contentScale,
            modifier = Modifier.fillMaxSize()
        )
    }
}