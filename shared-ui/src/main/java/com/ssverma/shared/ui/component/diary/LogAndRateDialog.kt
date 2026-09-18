package com.ssverma.shared.ui.component.diary

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.layout.ShowTimeBottomSheet
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.ui.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LogAndRateDialog(
    mediaId: Int,
    mediaType: MediaType,
    title: String,
    posterImageUrl: String,
    backdropImageUrl: String = "",
    releaseDate: String = "",
    tmdbRating: Float = 0f,
    existingEntry: DiaryEntry? = null,
    onDismiss: () -> Unit,
    onSave: (DiaryEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    var userRating by remember { mutableFloatStateOf(existingEntry?.userRating ?: 4.0f) }
    var review by remember { mutableStateOf(existingEntry?.review ?: "") }
    var isRewatch by remember { mutableStateOf(existingEntry?.isRewatch ?: false) }

    val resolvedPosterUrl = remember(posterImageUrl) {
        when {
            posterImageUrl.isBlank() -> ""
            posterImageUrl.startsWith("http://") || posterImageUrl.startsWith("https://") -> posterImageUrl
            else -> {
                val path =
                    if (posterImageUrl.startsWith("/")) posterImageUrl else "/$posterImageUrl"
                "https://image.tmdb.org/t/p/w500$path"
            }
        }
    }

    val resolvedBackdropUrl = remember(backdropImageUrl) {
        when {
            backdropImageUrl.isBlank() -> ""
            backdropImageUrl.startsWith("http://") || backdropImageUrl.startsWith("https://") -> backdropImageUrl
            else -> {
                val path =
                    if (backdropImageUrl.startsWith("/")) backdropImageUrl else "/$backdropImageUrl"
                "https://image.tmdb.org/t/p/w780$path"
            }
        }
    }

    ShowTimeBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 16.dp)
        ) {
            // Media Header
            LogAndRateMediaHeader(
                title = title,
                posterImageUrl = resolvedPosterUrl,
                mediaType = mediaType,
                releaseDate = releaseDate,
                tmdbRating = tmdbRating,
                isEditing = existingEntry != null,
                onClose = onDismiss
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Scrollable Content
            Column(
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                // Star Rating Section
                LogAndRateRatingSection(
                    userRating = userRating,
                    onRatingChanged = { userRating = it }
                )

                // Rewatch & Review Section
                LogAndRateReviewSection(
                    isRewatch = isRewatch,
                    onRewatchChange = { isRewatch = it },
                    review = review,
                    onReviewChange = { review = it }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Actions
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Button(
                    onClick = {
                        val entry = DiaryEntry(
                            id = existingEntry?.id ?: 0L,
                            mediaId = mediaId,
                            mediaType = mediaType,
                            title = title,
                            posterImageUrl = resolvedPosterUrl,
                            backdropImageUrl = resolvedBackdropUrl,
                            releaseDate = releaseDate,
                            tmdbRating = tmdbRating,
                            userRating = userRating,
                            review = review.trim(),
                            isRewatch = isRewatch,
                            loggedAt = existingEntry?.loggedAt ?: System.currentTimeMillis()
                        )
                        onSave(entry)
                    },
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier
                        .weight(1.5f)
                        .height(48.dp)
                ) {
                    Text(
                        text = if (existingEntry != null) {
                            stringResource(R.string.diary_dialog_update_action)
                        } else {
                            stringResource(R.string.diary_dialog_save_action)
                        },
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
