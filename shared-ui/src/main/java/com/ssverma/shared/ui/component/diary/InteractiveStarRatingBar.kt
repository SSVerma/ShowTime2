package com.ssverma.shared.ui.component.diary

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.StarHalf
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.ssverma.shared.ui.R

@Composable
fun InteractiveStarRatingBar(
    rating: Float,
    onRatingChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        modifier = modifier
    ) {
        for (i in 1..5) {
            val starValue = i.toFloat()
            val icon = when {
                rating >= starValue -> Icons.Rounded.Star
                rating >= starValue - 0.5f -> Icons.AutoMirrored.Rounded.StarHalf
                else -> Icons.Rounded.StarBorder
            }

            Icon(
                imageVector = icon,
                contentDescription = stringResource(R.string.diary_dialog_star_cd, i),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(32.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        val newRating = if (rating == starValue) {
                            starValue - 0.5f
                        } else {
                            starValue
                        }
                        onRatingChanged(newRating)
                    }
            )
        }
    }
}
