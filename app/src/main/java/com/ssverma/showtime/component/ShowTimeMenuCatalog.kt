package com.ssverma.showtime.component

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Diamond
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Explore
import androidx.compose.material.icons.rounded.Extension
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Keyboard
import androidx.compose.material.icons.rounded.NewReleases
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.People
import androidx.compose.material.icons.rounded.Policy
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Public
import androidx.compose.material.icons.rounded.TaskAlt
import androidx.compose.runtime.Immutable
import androidx.navigation3.runtime.NavKey
import androidx.compose.ui.graphics.vector.ImageVector
import com.ssverma.feature.account.navigation.BackupSyncNavKey
import com.ssverma.feature.library.navigation.BacklogChallengeNavKey
import com.ssverma.feature.library.navigation.CinemaDiaryNavKey
import com.ssverma.feature.library.navigation.CinemaReceiptNavKey
import com.ssverma.feature.library.navigation.CinephileWrappedNavKey
import com.ssverma.feature.library.navigation.TasteProfileNavKey
import com.ssverma.feature.match.navigation.MatchRoomNavKey
import com.ssverma.feature.movie.navigation.CinemaGameNavKey
import com.ssverma.feature.person.navigation.PersonHomeNavKey
import com.ssverma.showtime.R
import com.ssverma.showtime.feature.filter.navigation.UniversalDiscoveryNavKey
import com.ssverma.showtime.navigation.WhatsNewNavKey
import kotlin.reflect.KClass

@Immutable
internal data class ShowTimeMenuItem(
    @param:StringRes val labelResId: Int,
    val icon: ImageVector,
    val usePrimaryTint: Boolean = false,
    val targetKeyClass: KClass<out NavKey>? = null,
    val onClick: () -> Unit
)

@Immutable
internal data class ShowTimeMenuSection(
    @param:StringRes val titleResId: Int,
    val items: List<ShowTimeMenuItem>
)

internal fun buildShowTimeMenuSections(
    actions: ShowTimeMenuActions,
    includePeopleItem: Boolean,
    includeKeyboardShortcutsItem: Boolean
): List<ShowTimeMenuSection> {
    val exploreItems = buildList {
        add(
            ShowTimeMenuItem(
                labelResId = R.string.drawer_discover,
                icon = Icons.Rounded.Explore,
                targetKeyClass = UniversalDiscoveryNavKey::class,
                onClick = actions.onOpenDiscovery
            )
        )
        add(
            ShowTimeMenuItem(
                labelResId = R.string.drawer_movie_match,
                icon = Icons.Rounded.Favorite,
                targetKeyClass = MatchRoomNavKey::class,
                onClick = actions.onOpenMovieMatch
            )
        )
        if (includePeopleItem) {
            add(
                ShowTimeMenuItem(
                    labelResId = R.string.people,
                    icon = Icons.Rounded.People,
                    targetKeyClass = PersonHomeNavKey::class,
                    onClick = actions.onOpenPeople
                )
            )
        }
    }

    val cinephileItems = listOf(
        ShowTimeMenuItem(
            labelResId = R.string.drawer_cinema_diary,
            icon = Icons.AutoMirrored.Rounded.MenuBook,
            targetKeyClass = CinemaDiaryNavKey::class,
            onClick = actions.onOpenCinemaDiary
        ),
        ShowTimeMenuItem(
            labelResId = R.string.drawer_taste_profile,
            icon = Icons.Rounded.Psychology,
            targetKeyClass = TasteProfileNavKey::class,
            onClick = actions.onOpenTasteProfile
        ),
        ShowTimeMenuItem(
            labelResId = R.string.drawer_wrapped,
            icon = Icons.Rounded.EmojiEvents,
            targetKeyClass = CinephileWrappedNavKey::class,
            onClick = actions.onOpenWrapped
        ),
        ShowTimeMenuItem(
            labelResId = R.string.drawer_challenges,
            icon = Icons.Rounded.TaskAlt,
            targetKeyClass = BacklogChallengeNavKey::class,
            onClick = actions.onOpenBacklogChallenges
        ),
        ShowTimeMenuItem(
            labelResId = R.string.cinema_receipt,
            icon = Icons.AutoMirrored.Rounded.ReceiptLong,
            targetKeyClass = CinemaReceiptNavKey::class,
            onClick = actions.onOpenReceipt
        )
    )

    val dailyHabitItems = listOf(
        ShowTimeMenuItem(
            labelResId = R.string.daily_cinema_challenge,
            icon = Icons.Rounded.Extension,
            targetKeyClass = CinemaGameNavKey::class,
            onClick = actions.onOpenCinemaGame
        )
    )

    val syncItems = listOf(
        ShowTimeMenuItem(
            labelResId = R.string.cloud_backup_and_restore,
            icon = Icons.Rounded.CloudSync,
            targetKeyClass = BackupSyncNavKey::class,
            onClick = actions.onOpenBackup
        )
    )

    val preferenceItems = buildList {
        add(
            ShowTimeMenuItem(
                labelResId = R.string.pro_membership,
                icon = Icons.Rounded.Diamond,
                usePrimaryTint = true,
                onClick = actions.onOpenPro
            )
        )
        add(
            ShowTimeMenuItem(
                labelResId = R.string.appearance_theme,
                icon = Icons.Rounded.Palette,
                onClick = actions.onOpenTheme
            )
        )
        add(
            ShowTimeMenuItem(
                labelResId = R.string.localization_settings,
                icon = Icons.Rounded.Public,
                onClick = actions.onOpenLocalization
            )
        )
        if (includeKeyboardShortcutsItem) {
            add(
                ShowTimeMenuItem(
                    labelResId = R.string.keyboard_shortcuts_title,
                    icon = Icons.Rounded.Keyboard,
                    onClick = actions.onOpenKeyboardShortcuts
                )
            )
        }
    }

    val legalItems = listOf(
        ShowTimeMenuItem(
            labelResId = R.string.drawer_whats_new,
            icon = Icons.Rounded.NewReleases,
            usePrimaryTint = true,
            targetKeyClass = WhatsNewNavKey::class,
            onClick = actions.onOpenWhatsNew
        ),
        ShowTimeMenuItem(
            labelResId = R.string.about_showtime,
            icon = Icons.Rounded.Info,
            onClick = actions.onOpenAbout
        ),
        ShowTimeMenuItem(
            labelResId = R.string.privacy_policy,
            icon = Icons.Rounded.Policy,
            onClick = actions.onOpenPrivacy
        ),
        ShowTimeMenuItem(
            labelResId = R.string.terms_of_service,
            icon = Icons.Rounded.Description,
            onClick = actions.onOpenTerms
        ),
        ShowTimeMenuItem(
            labelResId = R.string.open_source_licenses,
            icon = Icons.Rounded.Extension,
            onClick = actions.onOpenLicenses
        )
    )

    return listOf(
        ShowTimeMenuSection(titleResId = R.string.drawer_section_explore, items = exploreItems),
        ShowTimeMenuSection(titleResId = R.string.drawer_section_cinephile, items = cinephileItems),
        ShowTimeMenuSection(titleResId = R.string.drawer_section_daily, items = dailyHabitItems),
        ShowTimeMenuSection(titleResId = R.string.sync_section, items = syncItems),
        ShowTimeMenuSection(titleResId = R.string.preferences_section, items = preferenceItems),
        ShowTimeMenuSection(titleResId = R.string.legal_section, items = legalItems)
    )
}
