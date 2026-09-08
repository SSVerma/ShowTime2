package com.ssverma.core.backup.di

import com.ssverma.core.backup.contributor.BackupContributor
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds

@Module
@InstallIn(SingletonComponent::class)
abstract class BackupContributorModule {
    @Multibinds
    abstract fun bindBackupContributors(): Set<BackupContributor>
}
