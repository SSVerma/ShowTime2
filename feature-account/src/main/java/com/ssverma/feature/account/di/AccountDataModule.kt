package com.ssverma.feature.account.di

import com.ssverma.feature.account.data.remote.AccountRemoteDataSource
import com.ssverma.feature.account.data.remote.DefaultAccountRemoteDataSource
import com.ssverma.feature.account.data.repository.DefaultAccountRepository
import com.ssverma.feature.account.domain.repository.AccountRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
internal abstract class AccountDataModule {
    @Binds
    abstract fun bindAccountRemoteDataSource(
        defaultAccountRemoteDataSource: DefaultAccountRemoteDataSource
    ): AccountRemoteDataSource

    @Binds
    abstract fun bindAccountRepository(
        defaultAccountRepository: DefaultAccountRepository
    ): AccountRepository
}