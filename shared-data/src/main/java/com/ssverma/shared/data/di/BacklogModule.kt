package com.ssverma.shared.data.di

import com.ssverma.shared.data.repository.BacklogRepositoryImpl
import com.ssverma.shared.domain.repository.BacklogRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class BacklogModule {

    @Binds
    @Singleton
    abstract fun bindBacklogRepository(
        backlogRepositoryImpl: BacklogRepositoryImpl
    ): BacklogRepository
}
