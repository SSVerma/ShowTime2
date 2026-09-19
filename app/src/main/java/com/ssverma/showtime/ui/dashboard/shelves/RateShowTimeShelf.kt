package com.ssverma.showtime.ui.dashboard.shelves

import android.os.Build
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.RateReview
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.core.ui.util.openPlayStore
import com.ssverma.core.ui.util.sendEmail
import com.ssverma.core.ui.util.shareText
import com.ssverma.shared.domain.utils.AppConfigConstants
import com.ssverma.showtime.R

fun LazyListScope.rateShowTimeShelf(
    modifier: Modifier = Modifier
) {
    item(key = "rate_showtime_shelf") {
        RateShowTimeCard(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
        )
    }
}

@Composable
fun RateShowTimeCard(
    modifier: Modifier = Modifier
) {
    var isDismissed by rememberSaveable { mutableStateOf(false) }
    var selectedRating by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current

    if (isDismissed) return

    Card(
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        ),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(MaterialTheme.spacing.medium)
        ) {
            // Header Row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Favorite,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))

                    Column {
                        Text(
                            text = stringResource(id = R.string.rate_shelf_title),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = stringResource(id = R.string.rate_shelf_subtitle),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = { isDismissed = true },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = stringResource(id = R.string.rate_shelf_dismiss_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(MaterialTheme.spacing.medium))

            // 5 Star Rating Row
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                for (star in 1..5) {
                    val isFilled = star <= selectedRating
                    Icon(
                        imageVector = if (isFilled) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                        contentDescription = stringResource(id = R.string.rate_shelf_star_cd, star),
                        tint = if (isFilled) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.outlineVariant
                        },
                        modifier = Modifier
                            .size(40.dp)
                            .padding(MaterialTheme.spacing.extraSmall)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = ripple(bounded = false, radius = 20.dp),
                                onClick = { selectedRating = star }
                            )
                    )
                }
            }

            // Dynamic Action Prompts based on rating
            AnimatedVisibility(
                visible = selectedRating > 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = MaterialTheme.spacing.small)
                ) {
                    if (selectedRating >= 4) {
                        Text(
                            text = stringResource(id = R.string.rate_shelf_high_rating_msg),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                        )
                        Button(
                            onClick = {
                                context.openPlayStore(
                                    marketUri = AppConfigConstants.PLAY_STORE_MARKET_URI,
                                    webFallbackUrl = AppConfigConstants.PLAY_STORE_URL
                                )
                                isDismissed = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.RateReview,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                            Text(text = stringResource(id = R.string.rate_shelf_rate_play_store))
                        }
                    } else {
                        Text(
                            text = stringResource(id = R.string.rate_shelf_low_rating_msg),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = MaterialTheme.spacing.small)
                        )
                        Button(
                            onClick = {
                                context.sendEmail(
                                    toEmail = AppConfigConstants.CONTACT_EMAIL,
                                    subject = context.getString(
                                        R.string.rate_shelf_feedback_subject,
                                        Build.VERSION.RELEASE
                                    )
                                )
                                isDismissed = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = stringResource(id = R.string.rate_shelf_send_feedback))
                        }
                    }
                }
            }

            // Secondary Action: Share with Friends
            Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))
            OutlinedButton(
                onClick = {
                    context.shareText(
                        text = context.getString(
                            R.string.rate_shelf_share_text,
                            AppConfigConstants.PLAY_STORE_URL
                        ),
                        title = context.getString(R.string.rate_shelf_share_chooser_title)
                    )
                },
                border = BorderStroke(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Rounded.Share,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(MaterialTheme.spacing.small))
                Text(
                    text = stringResource(id = R.string.rate_shelf_share_friends),
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
