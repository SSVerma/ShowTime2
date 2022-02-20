package com.ssverma.core.networking.config

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.networking.interceptor.ApplicationInterceptor
import com.ssverma.core.networking.interceptor.NetworkInterceptor
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import org.junit.Before
import org.junit.Test
import java.io.File

class OkHttpConfigApplierTest {

    private lateinit var okHttpBuilder: OkHttpClient.Builder

    @Before
    fun setUp() {
        okHttpBuilder = OkHttpClient.Builder()
    }

    @Test
    fun `verify config applier applies valid configs correctly`() {
        val fakeAppInterceptor = FakeAppInterceptor()
        val fakeNetworkInterceptor = FakeNetworkInterceptor()
        val testCache = Cache(File.createTempFile("pre", "suffix"), 1)

        val config = object : OkHttpConfig {
            override val applicationInterceptors: List<ApplicationInterceptor>
                get() = listOf(fakeAppInterceptor)
            override val networkInterceptors: List<NetworkInterceptor>
                get() = listOf(fakeNetworkInterceptor)
            override val cache: Cache
                get() = testCache
        }

        okHttpBuilder.applyConfig(config)

        assertThat(okHttpBuilder.interceptors().size).isEqualTo(1)
        assertThat(okHttpBuilder.interceptors()).contains(fakeAppInterceptor)

        assertThat(okHttpBuilder.networkInterceptors().size).isEqualTo(1)
        assertThat(okHttpBuilder.networkInterceptors()).contains(fakeNetworkInterceptor)

        val client = okHttpBuilder.build()

        assertThat(client.cache).isNotNull()
        assertThat(client.cache?.maxSize()).isEqualTo(testCache.maxSize())
        assertThat(client.cache?.directory).isEqualTo(testCache.directory)
    }

    @Test
    fun `verify config applier applies multiple interceptors correctly`() {
        val fakeAppInterceptorOne = FakeAppInterceptor()
        val fakeAppInterceptorTwo = FakeAppInterceptor()
        val fakeNetworkInterceptorOne = FakeNetworkInterceptor()
        val fakeNetworkInterceptorTwo = FakeNetworkInterceptor()

        val config = object : OkHttpConfig {
            override val applicationInterceptors: List<ApplicationInterceptor>
                get() = listOf(fakeAppInterceptorOne, fakeAppInterceptorTwo)
            override val networkInterceptors: List<NetworkInterceptor>
                get() = listOf(fakeNetworkInterceptorOne, fakeNetworkInterceptorTwo)
            override val cache: Cache?
                get() = null
        }

        okHttpBuilder.applyConfig(config)

        assertThat(okHttpBuilder.interceptors().size).isEqualTo(1)
        assertThat(okHttpBuilder.interceptors())
            .containsExactly(fakeAppInterceptorTwo)

        assertThat(okHttpBuilder.networkInterceptors().size).isEqualTo(1)
        assertThat(okHttpBuilder.networkInterceptors())
            .containsExactly(fakeNetworkInterceptorTwo)
    }

    @Test
    fun `verify config applier applies multiple same instances only once`() {
        val fakeAppInterceptor = FakeAppInterceptor()
        val fakeNetworkInterceptor = FakeNetworkInterceptor()

        val config = object : OkHttpConfig {
            override val applicationInterceptors: List<ApplicationInterceptor>
                get() = listOf(fakeAppInterceptor, fakeAppInterceptor)
            override val networkInterceptors: List<NetworkInterceptor>
                get() = listOf(fakeNetworkInterceptor, fakeNetworkInterceptor)
            override val cache: Cache?
                get() = null
        }

        //First time, when no interceptors installed
        okHttpBuilder.applyConfig(config)

        assertThat(okHttpBuilder.interceptors().size).isEqualTo(1)
        assertThat(okHttpBuilder.interceptors()).containsExactly(fakeAppInterceptor)

        assertThat(okHttpBuilder.networkInterceptors().size).isEqualTo(1)
        assertThat(okHttpBuilder.networkInterceptors()).containsExactly(fakeNetworkInterceptor)

        //Second time, when interceptors exist
        okHttpBuilder.applyConfig(config)

        assertThat(okHttpBuilder.interceptors().size).isEqualTo(1)
        assertThat(okHttpBuilder.interceptors()).containsExactly(fakeAppInterceptor)

        assertThat(okHttpBuilder.networkInterceptors().size).isEqualTo(1)
        assertThat(okHttpBuilder.networkInterceptors()).containsExactly(fakeNetworkInterceptor)
    }

    @Test
    fun `verify config applier removes previous interceptor and then add new one if has same types`() {
        val fakeAppInterceptorOne = FakeAppInterceptor()
        val fakeNetworkInterceptorOne = FakeNetworkInterceptor()

        val configOne = object : OkHttpConfig {
            override val applicationInterceptors: List<ApplicationInterceptor>
                get() = listOf(fakeAppInterceptorOne)
            override val networkInterceptors: List<NetworkInterceptor>
                get() = listOf(fakeNetworkInterceptorOne)
            override val cache: Cache?
                get() = null
        }

        //First time, when no interceptors installed
        okHttpBuilder.applyConfig(configOne)

        assertThat(okHttpBuilder.interceptors().size).isEqualTo(1)
        assertThat(okHttpBuilder.interceptors()).containsExactly(fakeAppInterceptorOne)

        assertThat(okHttpBuilder.networkInterceptors().size).isEqualTo(1)
        assertThat(okHttpBuilder.networkInterceptors()).containsExactly(fakeNetworkInterceptorOne)

        val fakeAppInterceptorTwo = FakeAppInterceptor()
        val fakeWowAppInterceptor = FakeWowAppInterceptor()
        val fakeNetworkInterceptorTwo = FakeNetworkInterceptor()

        val configTwo = object : OkHttpConfig {
            override val applicationInterceptors: List<ApplicationInterceptor>
                get() = listOf(fakeAppInterceptorTwo, fakeWowAppInterceptor)
            override val networkInterceptors: List<NetworkInterceptor>
                get() = listOf(fakeNetworkInterceptorTwo)
            override val cache: Cache?
                get() = null
        }

        //Second time, when interceptors exist and provide same type but new instance
        okHttpBuilder.applyConfig(configTwo)

        assertThat(okHttpBuilder.interceptors().size).isEqualTo(2)
        assertThat(okHttpBuilder.interceptors())
            .containsExactly(fakeAppInterceptorTwo, fakeWowAppInterceptor)

        assertThat(okHttpBuilder.networkInterceptors().size).isEqualTo(1)
        assertThat(okHttpBuilder.networkInterceptors()).containsExactly(fakeNetworkInterceptorTwo)
    }

    @Test
    fun `verify config applier does not apply invalid supplied interceptor`() {
        val config = object : OkHttpConfig {
            override val applicationInterceptors: List<ApplicationInterceptor>
                get() = emptyList()
            override val networkInterceptors: List<NetworkInterceptor>
                get() = emptyList()
            override val cache: Cache?
                get() = null
        }

        okHttpBuilder.applyConfig(config)

        assertThat(okHttpBuilder.interceptors()).isEmpty()
        assertThat(okHttpBuilder.networkInterceptors()).isEmpty()

        val client = okHttpBuilder.build()
        assertThat(client.cache).isNull()
    }

}

private class FakeAppInterceptor : ApplicationInterceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}

private class FakeWowAppInterceptor : ApplicationInterceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}

private class FakeNetworkInterceptor : NetworkInterceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        return chain.proceed(chain.request())
    }
}