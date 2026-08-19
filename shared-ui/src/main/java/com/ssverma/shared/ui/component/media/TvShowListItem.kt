package com.ssverma.shared.ui.component.media

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.image.NetworkImage
import com.ssverma.shared.domain.model.ProviderInfo
import com.ssverma.shared.domain.model.tv.TvShowPreview
import com.ssverma.shared.domain.utils.FormatterUtils
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import com.ssverma.shared.ui.component.WatchProviderTrigger
import com.ssverma.shared.ui.component.WatchProviderTriggerVariant

@Composable
fun TvShowListItem(
    tvShow: TvShowPreview,
    modifier: Modifier = Modifier,
    showRating: Boolean = true,
    indicator: (@Composable (TvShowPreview) -> Unit)? = null,
    overlayContent: (@Composable () -> Unit)? = null,
    onWatchProviderClick: ((ProviderInfo) -> Unit)? = null,
    onClick: (TvShowPreview) -> Unit,
) {
    OutlinedCard(
        onClick = { onClick(tvShow) },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.outlinedCardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        ),
        modifier = modifier
            .fillMaxWidth()
            .height(MediaItemDefaults.ListItemHeight)
    ) {
        Row(
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(TmdbPosterAspectRatio)
            ) {
                NetworkImage(
                    url = tvShow.posterImageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                indicator?.let { customIndicator ->
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    ) {
                        customIndicator(tvShow)
                    }
                }
            }

            Column(
                modifier = Modifier
                    .padding(start = 12.dp, top = 8.dp, end = 6.dp, bottom = 10.dp)
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Top Row: Title & Actions
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = tvShow.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 2.dp)
                    )

                    if (overlayContent != null) {
                        Box(modifier = Modifier.padding(start = 6.dp)) {
                            overlayContent()
                        }
                    } else if (onWatchProviderClick != null) {
                        Box(modifier = Modifier.padding(start = 6.dp)) {
                            WatchProviderTrigger(
                                mediaId = tvShow.id,
                                isMovie = false,
                                variant = WatchProviderTriggerVariant.Icon,
                                onWatchProviderClick = onWatchProviderClick
                            )
                        }
                    }
                }

                // Middle Row: Bullet-separated Metadata
                if (showRating && tvShow.voteCount > 0) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        if (tvShow.displayYear.isNotEmpty()) {
                            Text(
                                text = tvShow.displayYear,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "•",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "${FormatterUtils.formatRating(tvShow.voteAvgPercentage)} ${
                                FormatterUtils.formatVoteCount(
                                    tvShow.voteCount
                                )
                            }",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else if (tvShow.displayYear.isNotEmpty()) {
                    Text(
                        text = tvShow.displayYear,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Bottom Row: Overview
                Text(
                    text = tvShow.overview,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
