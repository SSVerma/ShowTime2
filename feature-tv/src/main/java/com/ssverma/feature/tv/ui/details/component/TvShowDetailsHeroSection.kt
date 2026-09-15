package com.ssverma.feature.tv.ui.details.component

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
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.foundation.Emphasize
import com.ssverma.feature.tv.R
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.DiscussionNavArgs
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.tv.TvShow
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

fun LazyListScope.tvShowDetailsHeroSection(
    tvShow: TvShow,
    hasReminder: Boolean,
    existingDiaryEntry: DiaryEntry?,
    hasDiaryEntries: Boolean,
    onBackPressed: () -> Unit,
    onPlayTrailer: () -> Unit,
    onOpenLogDialog: () -> Unit,
    onReminderClick: () -> Unit,
    onOpenDiscussions: (DiscussionNavArgs) -> Unit,
    onShare: () -> Unit,
    onShowFeedback: (ShowFeedbackArgs) -> Unit
) {
    item(key = "tv_hero_header", contentType = "hero_header") {
        BackdropHeader(
            backdropImageUrl = tvShow.backdropImageUrl,
            onCloseIconClick = onBackPressed,
            showTrailerFab = tvShow.primaryTrailer != null,
            onTrailerFabClick = onPlayTrailer,
            secondaryActions = {
                if (!tvShow.isUpcoming) {
                    BackdropActionButton(
                        onClick = onOpenLogDialog,
                        icon = if (hasDiaryEntries) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                        contentDescription = stringResource(id = SharedR.string.log_and_rate_cd)
                    )
                }
                MediaOmniActionMenu(
                    mediaId = tvShow.id,
                    mediaType = MediaType.Tv,
                    title = tvShow.title,
                    posterImageUrl = tvShow.posterImageUrl,
                    backdropImageUrl = tvShow.backdropImageUrl,
                    voteAvg = tvShow.voteAvg,
                    releaseDate = tvShow.firstAirDate?.toString().orEmpty(),
                    config = MediaOmniMenuConfig(
                        isUpcoming = tvShow.isUpcoming,
                        showReminder = false
                    ),
                    existingDiaryEntry = existingDiaryEntry,
                    onLogToDiary = onOpenLogDialog,
                    onOpenDiscussions = {
                        onOpenDiscussions(
                            DiscussionNavArgs(
                                mediaType = MediaType.Tv,
                                mediaId = tvShow.id,
                                title = tvShow.title,
                                posterImageUrl = tvShow.posterImageUrl,
                                backdropImageUrl = tvShow.backdropImageUrl
                            )
                        )
                    },
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
                if (tvShow.hasUpcomingEpisodes || hasReminder) {
                    BackdropActionButton(
                        onClick = onReminderClick,
                        icon = if (hasReminder) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone,
                        containerColor = if (hasReminder) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        contentColor = if (hasReminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        contentDescription = stringResource(id = if (hasReminder) SharedR.string.reminder_set else SharedR.string.remind_me)
                    )
                }
            }
        )
    }

    item(key = "tv_title", contentType = "title") {
        Text(
            text = tvShow.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(top = 24.dp)
        )
    }

    tvShow.tagline?.let { tagline ->
        item(key = "tv_tagline", contentType = "tagline") {
            Emphasize {
                Text(
                    text = tagline,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    fontStyle = FontStyle.Italic,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(top = 4.dp)
                )
            }
        }
    }

    item(key = "tv_highlights", contentType = "highlights") {
        Highlights(
            highlights = remember(tvShow) { tvShow.highlightedItems() },
            modifier = Modifier.padding(top = SectionVerticalSpacing)
        )
    }
}

private fun TvShow.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.emptyIfAbsent()
        ),
        Highlight(
            labelRes = R.string.first_air_date,
            value = displayFirstAirDate.orEmpty(),
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
            labelRes = R.string.seasons,
            value = seasonCount.toString()
        ),
        Highlight(
            labelRes = R.string.episode_number,
            value = episodeCount.toString()
        )
    )
}
