package com.ssverma.core.ads.di

import com.ssverma.core.ads.AppAdConfigProvider
import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.core.ads.quota.RewardManager
import com.ssverma.core.ads.quota.RewardManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AdConfigModule {

    @Binds
    abstract fun bindAdConfigProvider(
        appAdConfigProvider: AppAdConfigProvider
    ): AdConfigProvider

    @Binds
    abstract fun bindRewardManager(
        rewardManagerImpl: RewardManagerImpl
    ): RewardManager
}
