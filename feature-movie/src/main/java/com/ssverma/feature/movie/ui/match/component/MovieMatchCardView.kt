package com.ssverma.feature.movie.ui.match.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ssverma.core.image.NetworkImage
import com.ssverma.feature.movie.ui.match.MovieMatchColor
import com.ssverma.shared.domain.model.match.MovieMatchCard
import java.util.Locale

@Composable
fun MovieMatchCardView(
    card: MovieMatchCard,
    modifier: Modifier = Modifier,
    likeOverlayAlpha: Float = 0f,
    nopeOverlayAlpha: Float = 0f,
    onOpenDetails: () -> Unit = {}
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.25f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        modifier = modifier
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Poster Background
            NetworkImage(
                url = if (card.posterImageUrl.isNotBlank()) card.posterImageUrl else card.backdropImageUrl,
                contentDescription = card.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Cinematic Scrim Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                Color.Black.copy(alpha = 0.35f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.65f),
                                Color.Black.copy(alpha = 0.95f)
                            )
                        )
                    )
            )

            // Top Controls Bar (Rating & Info)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Star,
                            contentDescription = null,
                            tint = MovieMatchColor.RatingGold,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.1f", card.voteAvg),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = Color.Black.copy(alpha = 0.65f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.15f)),
                    modifier = Modifier.size(36.dp)
                ) {
                    IconButton(onClick = onOpenDetails) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Movie Details",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Bottom Content Section
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(20.dp)
                    .clickable { onOpenDetails() }
            ) {
                // Title
                Text(
                    text = card.title,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Release Year & Genres
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    card.releaseYear?.let { year ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = Color.White.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = year,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (card.genreNames.isNotEmpty()) {
                        Text(
                            text = card.genreNames.take(3).joinToString(" • "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.85f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Overview Preview
                if (card.overview.isNotBlank()) {
                    Text(
                        text = card.overview,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
            }

            // Stamp: LIKE (Fades in when dragging right)
            if (likeOverlayAlpha > 0.05f) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    border = BorderStroke(4.dp, MovieMatchColor.LikeGreen),
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.TopStart)
                        .graphicsLayer {
                            alpha = likeOverlayAlpha
                            rotationZ = -15f
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            tint = MovieMatchColor.LikeGreen,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIKE",
                            color = MovieMatchColor.LikeGreen,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }

            // Stamp: NOPE (Fades in when dragging left)
            if (nopeOverlayAlpha > 0.05f) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent,
                    border = BorderStroke(4.dp, MovieMatchColor.PassRed),
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.TopEnd)
                        .graphicsLayer {
                            alpha = nopeOverlayAlpha
                            rotationZ = 15f
                        }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = null,
                            tint = MovieMatchColor.PassRed,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "PASS",
                            color = MovieMatchColor.PassRed,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                }
            }
        }
    }
}
