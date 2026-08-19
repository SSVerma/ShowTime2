package com.ssverma.shared.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.component.ShimmerPlaceholder
import com.ssverma.shared.ui.TmdbPosterAspectRatio
import com.ssverma.shared.ui.component.media.MediaItemDefaults

@Composable
fun MediaItemShimmer(
    modifier: Modifier = Modifier,
    itemWidth: Dp = MediaItemDefaults.PosterWidth
) {
    Column(modifier = modifier.width(itemWidth)) {
        ShimmerPlaceholder(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(TmdbPosterAspectRatio),
            shape = MaterialTheme.shapes.large
        )
        Spacer(modifier = Modifier.height(8.dp))
        ShimmerPlaceholder(
            modifier = Modifier
                .fillMaxWidth(0.7f)
                .height(16.dp)
        )
    }
}

@Composable
fun MediaItemRowShimmer(
    modifier: Modifier = Modifier,
    itemCount: Int = 5,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(16.dp),
    itemWidth: Dp = MediaItemDefaults.PosterWidth
) {
    LazyRow(
        contentPadding = contentPadding,
        horizontalArrangement = horizontalArrangement,
        modifier = modifier
    ) {
        items(itemCount) {
            MediaItemShimmer(itemWidth = itemWidth)
        }
    }
}

@Composable
fun MediaListItemShimmer(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        ShimmerPlaceholder(
            modifier = Modifier
                .width(96.dp)
                .height(140.dp),
            shape = MaterialTheme.shapes.large
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(20.dp)
            )
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(14.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            ShimmerPlaceholder(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(40.dp)
            )
        }
    }
}
