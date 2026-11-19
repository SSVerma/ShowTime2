package com.ssverma.feature.community.data.di

import com.ssverma.feature.community.data.datasource.RatingSandboxProvider
import com.ssverma.feature.community.data.sandbox.DebugRatingSandboxProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DebugRatingSandboxModule {

    @Binds
    @Singleton
    abstract fun bindRatingSandboxProvider(
        impl: DebugRatingSandboxProvider
    ): RatingSandboxProvider
}
