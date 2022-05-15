package com.ssverma.shared.ui.component

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.icon.AppIcons

@Composable
fun BackdropNavigationAction(
    onIconClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector = AppIcons.ArrowBack
) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colors.surface.copy(alpha = 0.54f),
        modifier = modifier
            .statusBarsPadding()
            .padding(start = 16.dp, top = 8.dp)
            .size(40.dp)
    ) {
        IconButton(onClick = onIconClick) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colors.onSurface
            )
        }
    }
}