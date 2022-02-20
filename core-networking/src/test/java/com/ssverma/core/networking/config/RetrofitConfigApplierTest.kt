package com.ssverma.core.networking.config

import com.google.common.truth.Truth.assertThat
import com.ssverma.core.networking.adapter.ApiResponseCallAdaptorFactory
import com.ssverma.core.networking.adapter.FakeCallAdapterFactoryOne
import com.ssverma.core.networking.adapter.FakeCallAdapterFactoryTwo
import com.ssverma.core.networking.convertor.*
import org.junit.Before
import org.junit.Test
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class RetrofitConfigApplierTest {

    private lateinit var retrofitBuilder: Retrofit.Builder

    @Before
    fun setUp() {
        retrofitBuilder = Retrofit.Builder()
    }

    @Test
    fun `verify given annotated convertor factory applied correctly`() {
        val fooFactory = EnvelopeConvertorFactory.create(FooEnvelop::class.java)
        val barFactory = EnvelopeConvertorFactory.create(BarEnvelop::class.java)

        val config = TestRetrofitConfig(
            annotatedConvertorFactories = mapOf(
                FooFactory::class.java to fooFactory,
                BarFactory::class.java to barFactory
            )
        )

        retrofitBuilder.applyConfig(config)

        assertThat(retrofitBuilder.converterFactories()).hasSize(1)

        val addedFactory = retrofitBuilder.converterFactories().first()

        assertThat(addedFactory).isInstanceOf(AnnotatedConvertorFactory::class.java)

        val annotatedFactory = addedFactory as AnnotatedConvertorFactory

        assertThat(annotatedFactory.factories).hasSize(2)

        val expectedFactories = mapOf(
            FooFactory::class.java to fooFactory,
            BarFactory::class.java to barFactory
        )

        assertThat(annotatedFactory.factories)
            .containsExactlyEntriesIn(expectedFactories)
    }

    @Test
    fun `verify only one annotated convertor factory resides in convertor factories list`() {
        val fooFactory = EnvelopeConvertorFactory.create(FooEnvelop::class.java)

        val configOne = TestRetrofitConfig(
            annotatedConvertorFactories = mapOf(
                FooFactory::class.java to fooFactory
            )
        )

        retrofitBuilder.applyConfig(configOne)

        assertThat(retrofitBuilder.converterFactories()).hasSize(1)

        val addedFactoryOne = retrofitBuilder.converterFactories().first()
        assertThat(addedFactoryOne).isInstanceOf(AnnotatedConvertorFactory::class.java)

        val annotatedFactoryOne = addedFactoryOne as AnnotatedConvertorFactory
        assertThat(annotatedFactoryOne.factories).hasSize(1)

        val expectedFactories = mapOf(FooFactory::class.java to fooFactory)
        assertThat(annotatedFactoryOne.factories)
            .containsExactlyEntriesIn(expectedFactories)


        val barFactory = EnvelopeConvertorFactory.create(BarEnvelop::class.java)

        val configTwo = TestRetrofitConfig(
            annotatedConvertorFactories = mapOf(
                BarFactory::class.java to barFactory
            )
        )

        retrofitBuilder.applyConfig(configTwo)

        assertThat(retrofitBuilder.converterFactories()).hasSize(1)

        val addedFactoryTwo = retrofitBuilder.converterFactories().first()
        assertThat(addedFactoryOne).isInstanceOf(AnnotatedConvertorFactory::class.java)

        val annotatedFactoryTwo = addedFactoryTwo as AnnotatedConvertorFactory
        assertThat(annotatedFactoryTwo.factories).hasSize(2)

        val expectedFactoriesTwo = mapOf(
            FooFactory::class.java to fooFactory,
            BarFactory::class.java to barFactory
        )

        assertThat(annotatedFactoryTwo.factories)
            .containsExactlyEntriesIn(expectedFactoriesTwo)
    }

    @Test
    fun `verify given invalid annotated convertor factories do not apply`() {
        val config = TestRetrofitConfig(
            annotatedConvertorFactories = emptyMap()
        )

        retrofitBuilder.applyConfig(config)

        assertThat(retrofitBuilder.converterFactories()).hasSize(0)
    }

    @Test
    fun `verify given valid convertor factories applied correctly`() {
        val fooFactory = EnvelopeConvertorFactory.create(FooEnvelop::class.java)
        val barFactory = EnvelopeConvertorFactory.create(BarEnvelop::class.java)

        val config = TestRetrofitConfig(
            convertorFactories = listOf(fooFactory, barFactory)
        )

        retrofitBuilder.applyConfig(config)

        assertThat(retrofitBuilder.converterFactories()).hasSize(2)
        assertThat(retrofitBuilder.converterFactories()).containsExactly(fooFactory, barFactory)
    }

    @Test
    fun `verify multiple generic convertor factories applied correctly`() {
        val fooFactoryOne = EnvelopeConvertorFactory.create(FooEnvelop::class.java)

        val configOne = TestRetrofitConfig(
            convertorFactories = listOf(fooFactoryOne)
        )

        retrofitBuilder.applyConfig(configOne)

        assertThat(retrofitBuilder.converterFactories()).hasSize(1)
        assertThat(retrofitBuilder.converterFactories()).containsExactly(fooFactoryOne)

        val fooFactoryTwo = EnvelopeConvertorFactory.create(FooEnvelop::class.java)
        val barFactory = EnvelopeConvertorFactory.create(BarEnvelop::class.java)

        val configTwo = TestRetrofitConfig(
            convertorFactories = listOf(fooFactoryTwo, barFactory)
        )

        retrofitBuilder.applyConfig(configTwo)

        assertThat(retrofitBuilder.converterFactories()).hasSize(3)
        assertThat(retrofitBuilder.converterFactories())
            .containsExactly(fooFactoryOne, fooFactoryTwo, barFactory)
    }

    @Test
    fun `verify multiple non generic convertor factories applied correctly and updated if exists already`() {
        val fooFactory = EnvelopeConvertorFactory.create(FooEnvelop::class.java)
        val gsonFactoryOne = GsonConverterFactory.create()

        val configOne = TestRetrofitConfig(
            convertorFactories = listOf(fooFactory, gsonFactoryOne)
        )

        retrofitBuilder.applyConfig(configOne)

        assertThat(retrofitBuilder.converterFactories()).hasSize(2)
        assertThat(retrofitBuilder.converterFactories()).containsExactly(fooFactory, gsonFactoryOne)

        val gsonFactoryTwo = GsonConverterFactory.create()

        val configTwo = TestRetrofitConfig(
            convertorFactories = listOf(gsonFactoryTwo)
        )

        retrofitBuilder.applyConfig(configTwo)

        assertThat(retrofitBuilder.converterFactories()).hasSize(2)
        assertThat(retrofitBuilder.converterFactories())
            .containsExactly(fooFactory, gsonFactoryTwo)
    }

    @Test
    fun `verify newly added convertor factories added first in the convertor factory list`() {
        val fooFactory = EnvelopeConvertorFactory.create(FooEnvelop::class.java)

        val configOne = TestRetrofitConfig(
            convertorFactories = listOf(fooFactory)
        )

        retrofitBuilder.applyConfig(configOne)

        assertThat(retrofitBuilder.converterFactories()).hasSize(1)
        assertThat(retrofitBuilder.converterFactories()).containsExactly(fooFactory)

        val fooFactoryTwo = EnvelopeConvertorFactory.create(FooEnvelop::class.java)
        val fooFactoryThree = EnvelopeConvertorFactory.create(FooEnvelop::class.java)

        val configTwo = TestRetrofitConfig(
            convertorFactories = listOf(fooFactoryTwo, fooFactoryThree)
        )

        retrofitBuilder.applyConfig(configTwo)

        assertThat(retrofitBuilder.converterFactories()).hasSize(3)
        assertThat(retrofitBuilder.converterFactories())
            .containsExactly(fooFactoryTwo, fooFactoryThree, fooFactory).inOrder()
    }

    @Test
    fun `verify given valid call adapter factories applied correctly`() {
        val factoryOne = ApiResponseCallAdaptorFactory.create()
        val factoryTwo = FakeCallAdapterFactoryOne()

        val config = TestRetrofitConfig(
            callAdapterFactories = listOf(factoryOne, factoryTwo)
        )

        retrofitBuilder.applyConfig(config)

        assertThat(retrofitBuilder.callAdapterFactories()).hasSize(2)
        assertThat(retrofitBuilder.callAdapterFactories()).containsExactly(factoryOne, factoryTwo)
    }

    @Test
    fun `verify same type call adapter factories applied only once and also most last entry`() {
        val factoryOne = ApiResponseCallAdaptorFactory.create()
        val factoryTwo = ApiResponseCallAdaptorFactory.create()

        val config = TestRetrofitConfig(
            callAdapterFactories = listOf(factoryOne, factoryTwo)
        )

        retrofitBuilder.applyConfig(config)

        assertThat(retrofitBuilder.callAdapterFactories()).hasSize(1)
        assertThat(retrofitBuilder.callAdapterFactories()).containsExactly(factoryTwo)

        val factoryThree = ApiResponseCallAdaptorFactory.create()

        val configTwo = TestRetrofitConfig(
            callAdapterFactories = listOf(factoryThree)
        )

        retrofitBuilder.applyConfig(configTwo)

        assertThat(retrofitBuilder.callAdapterFactories()).hasSize(1)
        assertThat(retrofitBuilder.callAdapterFactories()).containsExactly(factoryThree)
    }

    @Test
    fun `verify given valid call adapter factories adds in correct order`() {
        val factoryOne = FakeCallAdapterFactoryOne()
        val factoryTwo = FakeCallAdapterFactoryTwo()

        val configOne = TestRetrofitConfig(
            callAdapterFactories = listOf(factoryOne, factoryTwo)
        )

        retrofitBuilder.applyConfig(configOne)

        assertThat(retrofitBuilder.callAdapterFactories()).hasSize(2)
        assertThat(retrofitBuilder.callAdapterFactories()).containsExactly(factoryOne, factoryTwo)

        val factoryThree = ApiResponseCallAdaptorFactory.create()

        val configTwo = TestRetrofitConfig(
            callAdapterFactories = listOf(factoryThree, factoryOne, factoryTwo)
        )

        retrofitBuilder.applyConfig(configTwo)

        assertThat(retrofitBuilder.callAdapterFactories()).hasSize(3)
        assertThat(retrofitBuilder.callAdapterFactories())
            .containsExactly(factoryThree, factoryOne, factoryTwo)
    }

    @Test
    fun `verify invalid call adapter factories ignored`() {
        val configOne = TestRetrofitConfig(
            callAdapterFactories = emptyList()
        )

        retrofitBuilder.applyConfig(configOne)

        assertThat(retrofitBuilder.callAdapterFactories()).hasSize(0)
    }
}

private class TestRetrofitConfig(
    override val annotatedConvertorFactories: Map<Class<out Annotation>, Converter.Factory> = emptyMap(),
    override val convertorFactories: List<Converter.Factory> = emptyList(),
    override val callAdapterFactories: List<CallAdapter.Factory> = emptyList()
) : RetrofitConfig