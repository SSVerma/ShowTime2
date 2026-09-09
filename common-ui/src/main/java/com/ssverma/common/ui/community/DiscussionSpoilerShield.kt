package com.ssverma.common.ui.community

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material.icons.rounded.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.shared.ui.R

@Composable
fun DiscussionSpoilerShield(
    content: String,
    isSpoilerRevealed: Boolean,
    onToggleSpoiler: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    AnimatedContent(
        targetState = isSpoilerRevealed,
        transitionSpec = { fadeIn() togetherWith fadeOut() },
        label = "SpoilerContentTransition",
        modifier = modifier
    ) { revealed ->
        if (!revealed) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(shape = RoundedCornerShape(size = 12.dp))
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                        shape = RoundedCornerShape(size = 12.dp)
                    )
                    .background(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
                    .clickable {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        onToggleSpoiler()
                    }
                    .padding(all = 14.dp)
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f),
                    modifier = Modifier.blur(radius = 12.dp)
                )

                Surface(
                    shape = RoundedCornerShape(size = 20.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    contentColor = MaterialTheme.colorScheme.onErrorContainer,
                    shadowElevation = 0.dp
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.WarningAmber,
                            contentDescription = null,
                            modifier = Modifier.size(size = 16.dp)
                        )
                        Spacer(modifier = Modifier.width(width = 6.dp))
                        Text(
                            text = stringResource(id = R.string.tap_to_reveal_spoiler),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        } else {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium.copy(lineHeight = 22.sp),
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(height = 6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(size = 8.dp))
                        .clickable {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            onToggleSpoiler()
                        }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.VisibilityOff,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(size = 14.dp)
                    )
                    Spacer(modifier = Modifier.width(width = 4.dp))
                    Text(
                        text = stringResource(id = R.string.tap_to_hide_spoiler),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
