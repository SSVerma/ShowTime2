package com.ssverma.core.billing.di

import com.ssverma.core.billing.sandbox.BillingSandboxProvider
import com.ssverma.core.billing.sandbox.DebugBillingSandboxProvider
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DebugBillingSandboxModule {

    @Binds
    @Singleton
    abstract fun bindBillingSandboxProvider(
        impl: DebugBillingSandboxProvider
    ): BillingSandboxProvider
}
