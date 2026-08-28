package com.ssverma.feature.account.di

import com.ssverma.feature.account.domain.seeder.DatabaseSeeder
import com.ssverma.feature.account.seeder.DebugDatabaseSeeder
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DebugDatabaseSeederModule {

    @Binds
    @Singleton
    abstract fun bindDatabaseSeeder(impl: DebugDatabaseSeeder): DatabaseSeeder
}
