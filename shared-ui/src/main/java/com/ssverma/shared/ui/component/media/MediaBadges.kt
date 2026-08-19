package com.ssverma.shared.ui.component.media

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.TrendingUp
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.shared.domain.utils.FormatterUtils
import com.ssverma.shared.ui.R

@Composable
fun ScoreBadge(
    score: Float,
    modifier: Modifier = Modifier
) {
    MediaBadge(
        text = FormatterUtils.formatRating(score),
        icon = Icons.Rounded.Star,
        iconContentDescription = stringResource(id = R.string.rating_icon_cd),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
    )
}

@Composable
fun DateBadge(
    dateText: String,
    modifier: Modifier = Modifier
) {
    val compactDateText = remember(dateText) {
        val parts = dateText.trim().split(" ")
        if (parts.size == 3 && parts[2].all { it.isDigit() }) {
            "${parts[0]} ${parts[1]}"
        } else {
            dateText
        }
    }

    MediaBadge(
        text = compactDateText,
        icon = Icons.Rounded.CalendarToday,
        iconContentDescription = stringResource(id = R.string.date_icon_cd),
        containerColor = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = modifier
    )
}

@Composable
fun TextBadge(
    text: String,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.large,
    containerColor: Color = MaterialTheme.colorScheme.secondaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Surface(
        shape = shape,
        color = containerColor,
        modifier = modifier
    ) {
        Text(
            text = text,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
fun TrendingBadge(
    popularityText: String,
    modifier: Modifier = Modifier
) {
    MediaBadge(
        text = popularityText,
        icon = Icons.Rounded.TrendingUp,
        iconContentDescription = stringResource(id = R.string.popularity_icon_cd),
        containerColor = MaterialTheme.colorScheme.errorContainer,
        contentColor = MaterialTheme.colorScheme.onErrorContainer,
        modifier = modifier
    )
}

@Composable
private fun MediaBadge(
    text: String,
    icon: ImageVector,
    iconContentDescription: String?,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = MaterialTheme.shapes.large
) {
    Surface(
        shape = shape,
        color = containerColor.copy(alpha = 0.9f),
        modifier = modifier
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
            horizontalArrangement = Arrangement.spacedBy(3.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = iconContentDescription,
                modifier = Modifier.size(12.dp),
                tint = contentColor
            )
            Text(
                text = text,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}
