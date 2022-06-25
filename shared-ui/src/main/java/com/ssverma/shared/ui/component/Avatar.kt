package com.ssverma.shared.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage

@Composable
fun Avatar(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    onClick: () -> Unit,
    borderWidth: Dp = AvatarDefaults.BorderWidth,
    borderSpacing: Dp = AvatarDefaults.BorderSpacing
) {
    Box(
        modifier = modifier
            .background(color = MaterialTheme.colors.surface, shape = CircleShape)
            .size(AvatarDefaults.Size)
            .border(
                width = borderWidth,
                color = MaterialTheme.colors.primaryVariant,
                shape = CircleShape
            )
            .padding(borderSpacing)
            .clip(CircleShape)
            .clickable { onClick() }
    ) {
        NetworkImage(
            url = imageUrl,
            contentDescription = contentDescription,
            contentScale = contentScale,
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
        )
    }
}

object AvatarDefaults {
    val Size = 48.dp
    val BorderWidth = 2.dp
    val BorderSpacing = 4.dp
}