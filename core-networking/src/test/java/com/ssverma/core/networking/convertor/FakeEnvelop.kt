package com.ssverma.core.networking.convertor

import com.google.gson.annotations.SerializedName
import com.squareup.moshi.Json

internal data class Foo(
    @SerializedName("foo_id")
    val id: Int,

    @SerializedName("foo_name")
    val name: String
)

internal data class FooMeta(
    @SerializedName("message")
    val message: String,

    @SerializedName("timestamp")
    val timeStamp: Long
)

internal data class FooEnvelop(
    @SerializedName("metadata")
    val meta: FooMeta,

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


internal data class FakeUser(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String
)

internal data class FakeUserMetadata(
    @SerializedName("message")
    val message: String,

    @SerializedName("timestamp")
    val timeStamp: Long
)

internal data class FakeUserEnvelop(
    @SerializedName("metadata")
    val meta: FakeUserMetadata,

    @SerializedName("user")
    override val response: FakeUser

) : Envelope<FakeUser>


internal data class FakeMoshiUser(
    @field:Json(name = "id")
    val id: Int,

    @field:Json(name = "name")
    val fullName: String
)