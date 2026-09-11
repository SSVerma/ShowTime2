package com.ssverma.feature.match.ui.component

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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.ssverma.feature.match.R
import com.ssverma.feature.match.ui.MovieMatchColor
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
    modifier: Modifier = Modifier,
    onRewindProPrompt: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    val topCard = cards.getOrNull(topCardIndex)
    val nextCard = cards.getOrNull(topCardIndex + 1)

    val offsetX = remember(topCardIndex) { Animatable(0f) }
    val offsetY = remember(topCardIndex) { Animatable(0f) }

    val likeAlpha = (offsetX.value / 160f).coerceIn(0f, 1f)
    val nopeAlpha = (-offsetX.value / 160f).coerceIn(0f, 1f)
    val rotationAngle = (offsetX.value / 24f).coerceIn(-20f, 20f)

    val likeScale = (1f + (0.22f * likeAlpha) - (0.05f * nopeAlpha)).coerceIn(0.85f, 1.25f)
    val nopeScale = (1f + (0.22f * nopeAlpha) - (0.05f * likeAlpha)).coerceIn(0.85f, 1.25f)

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
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = if (canRewind) 0.85f else 0.4f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = if (canRewind) 0.5f else 0.25f)
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(
                        onClick = {
                            if (canRewind) {
                                onRewind()
                            } else {
                                onRewindProPrompt()
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Undo,
                            contentDescription = stringResource(R.string.match_room_cd_undo),
                            tint = if (canRewind) MovieMatchColor.RewindAmber else MaterialTheme.colorScheme.onSurfaceVariant.copy(
                                alpha = 0.5f
                            ),
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Pass Button (Dislike)
                Surface(
                    shape = CircleShape,
                    color = MovieMatchColor.PassRed.copy(
                        alpha = (0.12f + 0.25f * nopeAlpha).coerceIn(
                            0f,
                            1f
                        )
                    ),
                    border = BorderStroke(
                        width = 1.5.dp + (1.dp * nopeAlpha),
                        color = MovieMatchColor.PassRed.copy(
                            alpha = (0.45f + 0.55f * nopeAlpha).coerceIn(
                                0f,
                                1f
                            )
                        )
                    ),
                    shadowElevation = 2.dp + (6.dp * nopeAlpha),
                    modifier = Modifier
                        .size(68.dp)
                        .graphicsLayer {
                            scaleX = nopeScale
                            scaleY = nopeScale
                        }
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                offsetX.animateTo(-1600f, tween(220))
                                onSwipe(topCard, SwipeDirection.PASS)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = stringResource(R.string.match_room_cd_pass),
                            tint = MovieMatchColor.PassRed,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }

                // Info / Details Button
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.85f),
                    border = BorderStroke(
                        1.dp,
                        MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
                    ),
                    modifier = Modifier.size(48.dp)
                ) {
                    IconButton(onClick = { onOpenDetails(topCard) }) {
                        Icon(
                            imageVector = Icons.Rounded.Info,
                            contentDescription = stringResource(R.string.match_room_cd_details),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // Like Button (Heart)
                Surface(
                    shape = CircleShape,
                    color = MovieMatchColor.LikeGreen.copy(
                        alpha = (0.12f + 0.25f * likeAlpha).coerceIn(
                            0f,
                            1f
                        )
                    ),
                    border = BorderStroke(
                        width = 1.5.dp + (1.dp * likeAlpha),
                        color = MovieMatchColor.LikeGreen.copy(
                            alpha = (0.45f + 0.55f * likeAlpha).coerceIn(
                                0f,
                                1f
                            )
                        )
                    ),
                    shadowElevation = 2.dp + (6.dp * likeAlpha),
                    modifier = Modifier
                        .size(68.dp)
                        .graphicsLayer {
                            scaleX = likeScale
                            scaleY = likeScale
                        }
                ) {
                    IconButton(
                        onClick = {
                            coroutineScope.launch {
                                offsetX.animateTo(1600f, tween(220))
                                onSwipe(topCard, SwipeDirection.LIKE)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Favorite,
                            contentDescription = stringResource(R.string.match_room_cd_like),
                            tint = MovieMatchColor.LikeGreen,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
        }
    }
}
