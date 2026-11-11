package com.ssverma.core.ai.di

import com.ssverma.core.ai.engine.MlKitOnDeviceAgentEngine
import com.ssverma.core.ai.engine.OnDeviceAgentEngine
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreAiModule {

    @Binds
    @Singleton
    abstract fun bindOnDeviceAgentEngine(
        impl: MlKitOnDeviceAgentEngine
    ): OnDeviceAgentEngine
}
