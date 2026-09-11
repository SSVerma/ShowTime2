package com.ssverma.feature.library.ui.share.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.feature.library.R
import com.ssverma.shared.domain.model.library.SecretSharedListItem
import com.ssverma.shared.ui.component.media.MediaCardFrostedActionButton
import com.ssverma.shared.ui.component.media.MediaCardRatingBadge
import com.ssverma.shared.ui.component.media.ShowTimeMediaGridCard

@Composable
fun SharedMediaGridCard(
    item: SecretSharedListItem,
    canRemove: Boolean,
    isOwner: Boolean,
    isAddedByCurrentUser: Boolean = false,
    onRemove: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val addedByName = item.addedByName
    val addedByLabel = when {
        isAddedByCurrentUser -> stringResource(R.string.secret_share_added_by_you)
        item.addedByUserId.isNullOrBlank() || addedByName.isNullOrBlank() || addedByName.equals(
            "Me",
            ignoreCase = true
        ) || addedByName == "Creator" -> {
            if (isOwner) null else stringResource(R.string.secret_share_added_by_creator)
        }

        else -> stringResource(R.string.secret_share_added_by, addedByName)
    }

    ShowTimeMediaGridCard(
        title = item.title,
        posterImageUrl = item.posterImageUrl,
        onClick = onClick,
        modifier = modifier,
        metadataHeight = 68.dp,
        topStartBadge = if (item.voteAvg > 0f) {
            { MediaCardRatingBadge(rating = item.voteAvg) }
        } else null,
        topEndAction = if (canRemove) {
            {
                MediaCardFrostedActionButton(
                    icon = Icons.Rounded.DeleteOutline,
                    contentDescription = stringResource(
                        R.string.secret_share_remove_item_cd,
                        item.title
                    ),
                    onClick = onRemove,
                    tint = MaterialTheme.colorScheme.error,
                    size = 28,
                    hasShadow = true
                )
            }
        } else null,
        subtitle = {
            Column(
                verticalArrangement = Arrangement.spacedBy(1.dp)
            ) {
                item.releaseYear?.let { year ->
                    Text(
                        text = year,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (!addedByLabel.isNullOrBlank()) {
                    Text(
                        text = addedByLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    )
}
