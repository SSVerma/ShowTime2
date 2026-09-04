package com.ssverma.shared.data.di

import com.ssverma.shared.data.repository.CinephileMilestoneRepositoryImpl
import com.ssverma.shared.domain.repository.CinephileMilestoneRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CinephileMilestoneModule {

    @Binds
    @Singleton
    abstract fun bindCinephileMilestoneRepository(
        impl: CinephileMilestoneRepositoryImpl
    ): CinephileMilestoneRepository
}
