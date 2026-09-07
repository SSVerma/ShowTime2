package com.ssverma.shared.data.di

import com.ssverma.shared.data.repository.MatchRoomRepositoryImpl
import com.ssverma.shared.domain.repository.MatchRoomRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MatchRoomModule {

    @Binds
    @Singleton
    abstract fun bindMatchRoomRepository(
        matchRoomRepositoryImpl: MatchRoomRepositoryImpl
    ): MatchRoomRepository
}
