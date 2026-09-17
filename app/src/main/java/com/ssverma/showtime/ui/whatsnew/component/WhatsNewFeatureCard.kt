package com.ssverma.showtime.ui.whatsnew.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.ssverma.core.ui.theme.spacing
import com.ssverma.shared.domain.model.feature.CinephileFeature
import com.ssverma.showtime.ui.whatsnew.WhatsNewFeature
import com.ssverma.feature.match.ui.component.MovieMatchIllustration
import com.ssverma.showtime.ui.whatsnew.illustration.BacklogChallengesIllustration
import com.ssverma.showtime.ui.whatsnew.illustration.CinemaDiaryIllustration
import com.ssverma.showtime.ui.whatsnew.illustration.CinemaReceiptIllustration
import com.ssverma.showtime.ui.whatsnew.illustration.CommunityListsIllustration
import com.ssverma.showtime.ui.whatsnew.illustration.DailyGameIllustration
import com.ssverma.showtime.ui.whatsnew.illustration.DiscoverIllustration
import com.ssverma.showtime.ui.whatsnew.illustration.MyListsIllustration
import com.ssverma.showtime.ui.whatsnew.illustration.TasteProfileIllustration
import com.ssverma.showtime.ui.whatsnew.illustration.WidgetsIllustration

@Composable
fun WhatsNewFeatureCard(
    feature: WhatsNewFeature,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = MaterialTheme.spacing.large)
    ) {
        // 1. Bespoke Cinema Animation Illustration (Strict 210.dp fixed viewport)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
        ) {
            when (feature.feature) {
                CinephileFeature.CINEMA_DIARY -> CinemaDiaryIllustration()
                CinephileFeature.CINEMA_RECEIPT -> CinemaReceiptIllustration()
                CinephileFeature.BACKLOG_CHALLENGES -> BacklogChallengesIllustration()
                CinephileFeature.HOME_SCREEN_WIDGETS -> WidgetsIllustration()
                CinephileFeature.MY_LISTS -> MyListsIllustration()
                CinephileFeature.DISCOVERY -> DiscoverIllustration()
                CinephileFeature.COMMUNITY_LISTS -> CommunityListsIllustration()
                CinephileFeature.MOVIE_MATCH -> MovieMatchIllustration()
                CinephileFeature.TASTE_PROFILE -> TasteProfileIllustration()
                CinephileFeature.DAILY_GAME -> DailyGameIllustration()
                else -> DiscoverIllustration()
            }
        }

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.large))

        // 2. Feature Title (Clean headline)
        Text(
            text = stringResource(id = feature.titleRes),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(MaterialTheme.spacing.small))

        // 3. Feature Tagline (Concise 2-line capacity, fixed height to prevent layout jumps)
        Box(
            contentAlignment = Alignment.TopCenter,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
        ) {
            Text(
                text = stringResource(id = feature.descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
