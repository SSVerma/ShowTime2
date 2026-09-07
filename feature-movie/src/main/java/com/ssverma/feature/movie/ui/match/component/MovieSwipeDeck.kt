package com.ssverma.feature.movie.ui.match.component

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Undo
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ssverma.feature.movie.ui.match.MovieMatchColor
import com.ssverma.shared.domain.model.match.MovieMatchCard
import com.ssverma.shared.domain.model.match.SwipeDirection
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun MovieSwipeDeck(
    cards: List<MovieMatchCard>,
    topCardIndex: Int,
    canRewind: Boolean,
    onSwipe: (card: MovieMatchCard, direction: SwipeDirection) -> Unit,
    onOpenDetails: (card: MovieMatchCard) -> Unit,
    onRewind: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val topCard = cards.getOrNull(topCardIndex)
    val nextCard = cards.getOrNull(topCardIndex + 1)

    val offsetX = remember(topCardIndex) { Animatable(0f) }
    val offsetY = remember(topCardIndex) { Animatable(0f) }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Swipeable Cards Deck Area
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            if (topCard == null) {
                // Empty Deck View
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "End of Deck!",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "You've reviewed all movies in this round.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                // Background Card (Peeks underneath)
                if (nextCard != null) {
                    val dragProgress = (abs(offsetX.value) / 400f).coerceIn(0f, 1f)
                    val backgroundScale = 0.94f + (0.06f * dragProgress)
                    val backgroundOffset = (14 * (1f - dragProgress)).dp

                    key(nextCard.id) {
                        MovieMatchCardView(
                            card = nextCard,
                            modifier = Modifier
                                .fillMaxWidth(0.92f)
                                .aspectRatio(0.68f)
                                .offset(y = backgroundOffset)
                                .graphicsLayer {
                                    scaleX = backgroundScale
                                    scaleY = backgroundScale
                                    alpha = 0.82f + (0.18f * dragProgress)
                                }
                        )
                    }
                }

                // Foreground Top Card (Draggable)
                val likeAlpha = (offsetX.value / 160f).coerceIn(0f, 1f)
                val nopeAlpha = (-offsetX.value / 160f).coerceIn(0f, 1f)
                val rotationAngle = (offsetX.value / 24f).coerceIn(-20f, 20f)

                key(topCard.id) {
                    MovieMatchCardView(
                        card = topCard,
                        likeOverlayAlpha = likeAlpha,
                        nopeOverlayAlpha = nopeAlpha,
                        onOpenDetails = { onOpenDetails(topCard) },
                        modifier = Modifier
                            .fillMaxWidth(0.92f)
                            .aspectRatio(0.68f)
                            .offset {
                                IntOffset(
                                    offsetX.value.roundToInt(),
                                    offsetY.value.roundToInt()
                                )
                            }
                            .graphicsLayer {
                                rotationZ = rotationAngle
                            }
                            .pointerInput(topCard.id) {
                                detectDragGestures(
                                    onDragEnd = {
                                        coroutineScope.launch {
                                            if (offsetX.value > 260f) {
                                                offsetX.animateTo(
                                                    targetValue = 1600f,
                                                    animationSpec = tween(220)
                                                )
                                                onSwipe(topCard, SwipeDirection.LIKE)
                                            } else if (offsetX.value < -260f) {
                                                offsetX.animateTo(
                                                    targetValue = -1600f,
                                                    animationSpec = tween(220)
                                                )
                                                onSwipe(topCard, SwipeDirection.PASS)
                                            } else {
                                                launch {
                                                    offsetX.animateTo(
                                                        0f,
                                                        spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                                    )
                                                }
                                                launch {
                                                    offsetY.animateTo(
                                                        0f,
                                                        spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        coroutineScope.launch {
                                            offsetX.snapTo(offsetX.value + dragAmount.x)
                                            offsetY.snapTo(offsetY.value + dragAmount.y * 0.45f)
                                        }
                                    }
                                )
                            }
                    )
                }
            }
        }

        // Programmatic Action Buttons
        if (topCard != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Rewind / Undo
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (canRewind) 0.8f else 0.3f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(
                        onClick = onRewind,
                        enabled = canRewind
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Undo,
                            contentDescription = "Undo Last Swipe",
                            tint = if (canRewind) MovieMatchColor.RewindAmber else Color.Gray,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Pass Button (Dislike)
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            offsetX.animateTo(-1600f, tween(220))
                            onSwipe(topCard, SwipeDirection.PASS)
                        }
                    },
                    shape = CircleShape,
                    containerColor = MovieMatchColor.PassRed.copy(alpha = 0.15f),
                    contentColor = MovieMatchColor.PassRed,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                    modifier = Modifier.size(68.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Pass Movie",
                        modifier = Modifier.size(34.dp)
                    )
                }

                // Info / Details Button
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(onClick = { onOpenDetails(topCard) }) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = "Movie Details",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Like Button (Heart)
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            offsetX.animateTo(1600f, tween(220))
                            onSwipe(topCard, SwipeDirection.LIKE)
                        }
                    },
                    shape = CircleShape,
                    containerColor = MovieMatchColor.LikeGreen.copy(alpha = 0.15f),
                    contentColor = MovieMatchColor.LikeGreen,
                    elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 2.dp),
                    modifier = Modifier.size(68.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Favorite,
                        contentDescription = "Like Movie",
                        modifier = Modifier.size(34.dp)
                    )
                }
            }
        }
    }
}
