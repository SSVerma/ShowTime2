package com.ssverma.showtime.ui.whatsnew.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.feature.CinephileFeature
import com.ssverma.showtime.R
import com.ssverma.showtime.ui.whatsnew.WhatsNewFeature

@Composable
fun WhatsNewPageIndicator(
    pageCount: Int,
    currentPage: Int,
    features: List<WhatsNewFeature>,
    exploredFeatures: Set<CinephileFeature>,
    onIndicatorClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val indicatorCd = stringResource(
        id = R.string.whats_new_page_indicator_cd,
        currentPage + 1,
        pageCount
    )

    Row(
        horizontalArrangement = Arrangement.spacedBy(MaterialTheme.spacing.small),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier.semantics {
            contentDescription = indicatorCd
        }
    ) {
        for (index in 0 until pageCount) {
            val isCurrent = index == currentPage
            val feature = features.getOrNull(index)?.feature
            val isExplored = feature != null && feature in exploredFeatures

            val width by animateDpAsState(
                targetValue = if (isCurrent) 24.dp else 8.dp,
                animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
                label = "indicator_width_$index"
            )

            val color by animateColorAsState(
                targetValue = when {
                    isCurrent -> MaterialTheme.colorScheme.primary
                    isExplored -> MaterialTheme.colorScheme.primary.copy(alpha = 0.65f)
                    else -> MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                },
                animationSpec = tween(durationMillis = 300),
                label = "indicator_color_$index"
            )

            Box(
                modifier = Modifier
                    .height(8.dp)
                    .width(width)
                    .clip(CircleShape)
                    .background(color)
                    .clickable { onIndicatorClick(index) }
            )
        }
    }
}
