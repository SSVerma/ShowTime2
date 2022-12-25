package com.ssverma.feature.account.data.local.serializer

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.protobuf.InvalidProtocolBufferException
import com.ssverma.showtime.UserAccount
import java.io.InputStream
import java.io.OutputStream

object UserAccountSerializer : Serializer<UserAccount> {
    override val defaultValue: UserAccount = UserAccount.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): UserAccount {
        try {
            return UserAccount.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto", exception)
        }

    }

    override suspend fun writeTo(t: UserAccount, output: OutputStream) {
        t.writeTo(output)
    }
}
