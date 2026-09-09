package com.ssverma.shared.ads.di

import com.ssverma.core.ads.config.AdConfigProvider
import com.ssverma.shared.ads.AppAdConfigProvider
import com.ssverma.shared.ads.quota.RewardManager
import com.ssverma.shared.ads.quota.RewardManagerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SharedAdsModule {

    @Binds
    @Singleton
    abstract fun bindAdConfigProvider(
        appAdConfigProvider: AppAdConfigProvider
    ): AdConfigProvider

    @Binds
    @Singleton
    abstract fun bindRewardManager(
        rewardManagerImpl: RewardManagerImpl
    ): RewardManager
}
