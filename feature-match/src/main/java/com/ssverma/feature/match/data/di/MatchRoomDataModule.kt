package com.ssverma.feature.match.data.di

import com.ssverma.feature.match.data.repository.MatchRoomRepositoryImpl
import com.ssverma.shared.domain.repository.MatchRoomRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class MatchRoomDataModule {

    @Binds
    @Singleton
    abstract fun bindMatchRoomRepository(
        matchRoomRepositoryImpl: MatchRoomRepositoryImpl
    ): MatchRoomRepository
}
