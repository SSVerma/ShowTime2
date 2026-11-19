package com.ssverma.feature.tv.ui.details.component

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.style.TextAlign
import com.ssverma.core.ui.foundation.Emphasize
import com.ssverma.core.ui.theme.spacing
import com.ssverma.feature.tv.R
import com.ssverma.shared.domain.model.MediaType
import com.ssverma.shared.domain.model.community.DiscussionNavArgs
import com.ssverma.shared.domain.model.diary.DiaryEntry
import com.ssverma.shared.domain.model.tv.TvShow
import com.ssverma.shared.ui.component.BackdropActionButton
import com.ssverma.shared.ui.component.BackdropHeader
import com.ssverma.shared.ui.component.Highlight
import com.ssverma.shared.ui.component.Highlights
import com.ssverma.shared.ui.component.community.CommunityRatingBadge
import com.ssverma.shared.ui.component.community.CommunityRatingUiModel
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
    onBackPressed: () -> Unit,
    onPlayTrailer: () -> Unit,
    onOpenLogDialog: () -> Unit,
    onReminderClick: () -> Unit,
    onOpenDiscussions: (DiscussionNavArgs) -> Unit,
    onShare: () -> Unit,
    onShowFeedback: (ShowFeedbackArgs) -> Unit,
    communityRating: CommunityRatingUiModel? = null,
    onCommunityRatingClick: () -> Unit = {},
) {
    item(key = "tv_hero_header", contentType = "hero_header") {
        BackdropHeader(
            backdropImageUrl = tvShow.backdropImageUrl,
            onCloseIconClick = onBackPressed,
            showTrailerFab = tvShow.primaryTrailer != null,
            onTrailerFabClick = onPlayTrailer,
            topTrailingContent = {
                if (!tvShow.isUpcoming) {
                    CommunityRatingBadge(
                        rating = communityRating,
                        onClick = onCommunityRatingClick,
                    )
                }
            },
            secondaryActions = {
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
                        showReminder = false,
                    ),
                    existingDiaryEntry = existingDiaryEntry,
                    onLogToDiary = onOpenLogDialog,
                    onRateClick = onCommunityRatingClick,
                    onOpenDiscussions = {
                        onOpenDiscussions(
                            DiscussionNavArgs(
                                mediaType = MediaType.Tv,
                                mediaId = tvShow.id,
                                title = tvShow.title,
                                posterImageUrl = tvShow.posterImageUrl,
                                backdropImageUrl = tvShow.backdropImageUrl,
                            ),
                        )
                    },
                    onShare = onShare,
                    onShowFeedback = onShowFeedback,
                    actionContent = { onClick ->
                        BackdropActionButton(
                            onClick = onClick,
                            icon = Icons.Rounded.Add,
                            contentDescription = stringResource(id = SharedR.string.more_options_cd),
                        )
                    },
                )
                if (tvShow.hasUpcomingEpisodes || hasReminder) {
                    BackdropActionButton(
                        onClick = onReminderClick,
                        icon = if (hasReminder) Icons.Rounded.NotificationsActive else Icons.Rounded.NotificationsNone,
                        containerColor = if (hasReminder) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        contentColor = if (hasReminder) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                        contentDescription = stringResource(id = if (hasReminder) SharedR.string.reminder_set else SharedR.string.remind_me),
                    )
                }
            },
        )
    }

    item(key = "tv_title", contentType = "title") {
        Text(
            text = tvShow.title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MaterialTheme.spacing.medium)
                .padding(top = MaterialTheme.spacing.large),
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
                        .padding(horizontal = MaterialTheme.spacing.medium)
                        .padding(top = MaterialTheme.spacing.extraSmall),
                )
            }
        }
    }

    item(key = "tv_highlights", contentType = "highlights") {
        Highlights(
            highlights = remember(tvShow) { tvShow.highlightedItems() },
            modifier = Modifier.padding(top = SectionVerticalSpacing),
        )
    }
}

private fun TvShow.highlightedItems(): List<Highlight> {
    return listOf(
        Highlight(
            labelRes = R.string.rating,
            value = voteAvg.emptyIfAbsent(),
        ),
        Highlight(
            labelRes = R.string.first_air_date,
            value = displayFirstAirDate.orEmpty(),
        ),
        Highlight(
            labelRes = R.string.status,
            value = status,
        ),
        Highlight(
            labelRes = R.string.language,
            value = originalLanguage,
        ),
        Highlight(
            labelRes = R.string.seasons,
            value = seasonCount.toString(),
        ),
        Highlight(
            labelRes = R.string.episode_number,
            value = episodeCount.toString(),
        ),
    )
}
