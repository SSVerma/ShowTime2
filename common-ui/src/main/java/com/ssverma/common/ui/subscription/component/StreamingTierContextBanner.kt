package com.ssverma.common.ui.subscription.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.shared.ui.R

@Composable
fun StreamingTierContextBanner(
    isProActive: Boolean,
    isPassActive: Boolean,
    modifier: Modifier = Modifier
) {
    val containerColor = when {
        isProActive -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
        isPassActive -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
    }
    val borderColor = when {
        isProActive -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        isPassActive -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = containerColor,
        border = BorderStroke(1.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = when {
                    isProActive -> MaterialTheme.colorScheme.primary
                    isPassActive -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.surfaceContainerHigh
                },
                modifier = Modifier.size(36.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when {
                            isProActive -> Icons.Rounded.Star
                            isPassActive -> Icons.Rounded.AutoAwesome
                            else -> Icons.Rounded.Tv
                        },
                        contentDescription = null,
                        tint = when {
                            isProActive -> MaterialTheme.colorScheme.onPrimary
                            isPassActive -> MaterialTheme.colorScheme.onTertiary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        },
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = when {
                        isProActive -> stringResource(R.string.streaming_subscriptions_pro_title)
                        isPassActive -> stringResource(R.string.streaming_subscriptions_pass_title)
                        else -> stringResource(R.string.streaming_subscriptions_free_title)
                    },
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = when {
                        isProActive -> MaterialTheme.colorScheme.onPrimaryContainer
                        isPassActive -> MaterialTheme.colorScheme.onTertiaryContainer
                        else -> MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = when {
                        isProActive -> stringResource(R.string.streaming_subscriptions_pro_desc)
                        isPassActive -> stringResource(R.string.streaming_subscriptions_pass_desc)
                        else -> stringResource(R.string.streaming_subscriptions_free_desc)
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = when {
                        isProActive -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                        isPassActive -> MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.85f)
                        else -> MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}
