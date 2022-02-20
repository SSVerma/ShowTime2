package com.ssverma.core.networking.convertor

import com.google.gson.annotations.SerializedName

internal data class Foo(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

internal data class FooEnvelop(
    @SerializedName("foo")
    override val response: Foo
) : Envelope<Foo>

internal data class Bar(
    @SerializedName("id")
    val id: Int,

    @SerializedName("address")
    val address: String
)

internal data class BarEnvelop(
    @SerializedName("bar")
    override val response: Bar
) : Envelope<Bar>