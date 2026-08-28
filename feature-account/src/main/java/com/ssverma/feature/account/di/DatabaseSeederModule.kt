package com.ssverma.feature.account.di

import com.ssverma.feature.account.domain.seeder.DatabaseSeeder
import dagger.BindsOptionalOf
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
interface DatabaseSeederModule {

    @BindsOptionalOf
    fun optionalDatabaseSeeder(): DatabaseSeeder
}
