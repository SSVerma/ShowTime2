package com.ssverma.shared.ui.component

import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssverma.core.image.NetworkImage
import com.ssverma.core.navigation.nav3.LocalNavAnimatedVisibilityScope
import com.ssverma.core.navigation.nav3.LocalSharedTransitionScope

fun personSharedContentKey(personId: Int): String = "person_avatar_$personId"

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun Avatar(
    imageUrl: String,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Crop,
    onClick: () -> Unit,
    borderWidth: Dp = AvatarDefaults.BorderWidth,
    borderColor: Color = MaterialTheme.colorScheme.primary,
    borderSpacing: Dp = AvatarDefaults.BorderSpacing,
    enableSharedTransition: Boolean = false,
    sharedContentKey: Any? = null
) {
    val sharedTransitionScope = LocalSharedTransitionScope.current
    val animatedVisibilityScope = LocalNavAnimatedVisibilityScope.current

    val sharedModifier =
        if (enableSharedTransition && sharedTransitionScope != null && animatedVisibilityScope != null && sharedContentKey != null) {
            with(sharedTransitionScope) {
                Modifier.sharedElement(
                    sharedContentState = rememberSharedContentState(key = sharedContentKey),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ ->
                        spring(
                            dampingRatio = 0.8f,
                            stiffness = 380f
                        )
                    },
                    clipInOverlayDuringTransition = OverlayClip(CircleShape),
                    zIndexInOverlay = 1f
                )
            }
        } else {
            Modifier
        }

    Box(
        modifier = modifier
            .then(sharedModifier)
            .background(color = MaterialTheme.colorScheme.surface, shape = CircleShape)
            .size(AvatarDefaults.Size)
            .border(
                width = borderWidth,
                color = borderColor,
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
