package com.ssverma.shared.data.backup.di

import com.ssverma.core.backup.contributor.BackupContributor
import com.ssverma.shared.data.backup.contributors.AppPreferencesBackupContributor
import com.ssverma.shared.data.backup.contributors.BacklogBackupContributor
import com.ssverma.shared.data.backup.contributors.CinemaDiaryBackupContributor
import com.ssverma.shared.data.backup.contributors.CinemaGameBackupContributor
import com.ssverma.shared.data.backup.contributors.CustomListsBackupContributor
import com.ssverma.shared.data.backup.contributors.FavoritesBackupContributor
import com.ssverma.shared.data.backup.contributors.ShowProgressBackupContributor
import com.ssverma.shared.data.backup.contributors.WatchHistoryBackupContributor
import com.ssverma.shared.data.backup.contributors.WatchlistBackupContributor
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupContributorsDataModule {

    @Binds
    @IntoSet
    abstract fun bindFavoritesBackupContributor(
        impl: FavoritesBackupContributor
    ): BackupContributor

    @Binds
    @IntoSet
    abstract fun bindWatchlistBackupContributor(
        impl: WatchlistBackupContributor
    ): BackupContributor

    @Binds
    @IntoSet
    abstract fun bindWatchHistoryBackupContributor(
        impl: WatchHistoryBackupContributor
    ): BackupContributor

    @Binds
    @IntoSet
    abstract fun bindCustomListsBackupContributor(
        impl: CustomListsBackupContributor
    ): BackupContributor

    @Binds
    @IntoSet
    abstract fun bindCinemaDiaryBackupContributor(
        impl: CinemaDiaryBackupContributor
    ): BackupContributor

    @Binds
    @IntoSet
    abstract fun bindShowProgressBackupContributor(
        impl: ShowProgressBackupContributor
    ): BackupContributor

    @Binds
    @IntoSet
    abstract fun bindBacklogBackupContributor(
        impl: BacklogBackupContributor
    ): BackupContributor

    @Binds
    @IntoSet
    abstract fun bindCinemaGameBackupContributor(
        impl: CinemaGameBackupContributor
    ): BackupContributor

    @Binds
    @IntoSet
    abstract fun bindAppPreferencesBackupContributor(
        impl: AppPreferencesBackupContributor
    ): BackupContributor
}
