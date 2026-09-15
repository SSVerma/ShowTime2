package com.ssverma.feature.movie.ui.details.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.ssverma.core.ui.foundation.Emphasize
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.movie.R
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.DiscussionNavArgs
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.movie.Movie
import com.ssverma.shared.domain.utils.DateUtils
import com.ssverma.shared.ui.component.BackdropActionButton
import com.ssverma.shared.ui.component.BackdropHeader
import com.ssverma.shared.ui.component.Highlight
import com.ssverma.shared.ui.component.Highlights
import com.ssverma.shared.ui.component.media.ShowFeedbackArgs
import com.ssverma.shared.ui.component.media.menu.MediaOmniActionMenu
import com.ssverma.shared.ui.component.media.menu.MediaOmniMenuConfig
import com.ssverma.shared.ui.component.section.SectionDefaults.SectionVerticalSpacing
import com.ssverma.shared.ui.emptyIfAbsent
import com.ssverma.shared.ui.R as SharedR

fun LazyListScope.movieDetailsHeroSection(
    movie: Movie,
    hasReminder: Boolean,
    existingDiaryEntry: DiaryEntry?,
    hasDiaryEntries: Boolean,
    onBackPressed: () -> Unit,
    onPlayTrailer: (Movie) -> Unit,
    onOpenLogDialog: () -> Unit,
    onReminderClick: () -> Unit,
    onOpenDiscussions: () -> Unit,
    onShare: () -> Unit,
    onShowFeedback: (ShowFeedbackArgs) -> Unit,
    modifier: Modifier = Modifier
) {
    item(key = "movie_details_backdrop_header", contentType = "backdrop_header") {
        BackdropHeader(
            backdropImageUrl = movie.backdropImageUrl,
            onCloseIconClick = onBackPressed,
            showTrailerFab = movie.primaryTrailer != null,
            onTrailerFabClick = { onPlayTrailer(movie) },
            secondaryActions = {
                if (!movie.isUpcoming) {
                    BackdropActionButton(
                        onClick = onOpenLogDialog,
                        icon = if (hasDiaryEntries) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = stringResource(id = SharedR.string.log_and_rate_cd)
                    )
                }
                MediaOmniActionMenu(
                    mediaId = movie.id,
                    mediaType = MediaType.Movie,
                    title = movie.title,
                    posterImageUrl = movie.posterImageUrl,
                    backdropImageUrl = movie.backdropImageUrl,
                    voteAvg = movie.voteAvg,
                    releaseDate = movie.releaseDate?.toString().orEmpty(),
                    config = MediaOmniMenuConfig(
                        isUpcoming = movie.isUpcoming,
                        showReminder = false
                    ),
                    existingDiaryEntry = existingDiaryEntry,
                    onLogToDiary = onOpenLogDialog,
                    onOpenDiscussions = onOpenDiscussions,
                    onShare = onShare,
                    onShowFeedback = onShowFeedback,
                    actionContent = { onClick ->
                        BackdropActionButton(
                            onClick = onClick,
                            icon = Icons.Rounded.Add,
                            contentDescription = stringResource(id = SharedR.string.more_options_cd)
                        )
                    }
                )
                if (movie.isUpcoming || hasReminder) {
                    BackdropActionButton(
                        onClick = onReminderClick,
                        icon = if (hasReminder) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone,
                        containerColor = if (hasReminder) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        contentColor = if (hasReminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        contentDescription = stringResource(
                            id = if (hasReminder) SharedR.string.reminder_set else SharedR.string.remind_me
                        )
                    )
                }
            },
            modifier = modifier
        )
    }

    item(key = "movie_details_title", contentType = "movie_title") {
        Text(
            text = movie.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .padding(top = MaterialTheme.spacing.large)
        )
    }

    movie.tagline?.let { tagline ->
        item(key = "movie_details_tagline", contentType = "movie_tagline") {
            Emphasize {
                Text(
                    text = tagline,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .padding(top = MaterialTheme.spacing.extraSmall)
                )
            }
        }
    }

    item(key = "movie_details_highlights", contentType = "movie_highlights") {
        Highlights(
            highlights = remember(movie) { movie.highlightedItems() },
            modifier = Modifier.padding(top = SectionVerticalSpacing)
        )
    }
}

private fun Movie.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.emptyIfAbsent()
        ),
        Highlight(
            labelRes = R.string.release_date,
            value = displayReleaseDate.orEmpty()
        ),
        Highlight(
            labelRes = R.string.status,
            value = status
        ),
        Highlight(
            labelRes = R.string.language,
            value = originalLanguage
        ),
        Highlight(
            labelRes = R.string.runtime,
            value = if (runtime == 0) 0.emptyIfAbsent() else DateUtils.formatMinutes(runtime)
        ),
        Highlight(
            labelRes = R.string.revenue,
            value = if (revenue == 0L) 0.emptyIfAbsent() else "$$revenue"
        )
    )
}
