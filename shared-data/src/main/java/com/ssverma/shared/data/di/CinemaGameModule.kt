package com.ssverma.shared.data.di

import com.ssverma.shared.data.repository.CinemaGameRepositoryImpl
import com.ssverma.shared.domain.repository.CinemaGameRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CinemaGameModule {

    @Binds
    @Singleton
    abstract fun bindCinemaGameRepository(
        cinemaGameRepositoryImpl: CinemaGameRepositoryImpl
    ): CinemaGameRepository
}
