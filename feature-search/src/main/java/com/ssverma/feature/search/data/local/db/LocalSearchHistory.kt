package com.ssverma.feature.search.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(
    tableName = SearchContract.History.TableName
)
data class LocalSearchHistory(
    @PrimaryKey
    @ColumnInfo(name = SearchContract.History.ColId)
    val id: Int,

    @ColumnInfo(name = SearchContract.History.ColName)
    val name: String,

    @ColumnInfo(name = SearchContract.History.ColMediaType)
    val mediaType: Int,

    @ColumnInfo(name = SearchContract.History.ColTimestamp)
    val timestamp: Long
)