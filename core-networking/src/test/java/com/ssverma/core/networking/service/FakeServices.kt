package com.ssverma.core.networking.service

import com.ssverma.core.networking.adapter.ApiResponse
import com.ssverma.core.networking.convertor.*
import retrofit2.http.GET

internal interface FakeUserApiService {
    @GET("/fakeUser")
    suspend fun getFakeUser(): ApiResponse<FakeUser, Any>

    @GET("/fakeEnvelopedUser")
    @FakeUserEnvelopConvertor
    suspend fun getEnvelopedFakeUser(): ApiResponse<FakeUser, Any>

    @GET("/fakeEnvelopedFoo")
    @FakeFooEnvelopConvertor
    suspend fun getEnvelopedFoo(): ApiResponse<Foo, Any>

    @GET("/fakeMoshiUser")
    @FakeMoshiConvertor
    suspend fun getFakeMoshiUser(): ApiResponse<FakeMoshiUser, Any>
}