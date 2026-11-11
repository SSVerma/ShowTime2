package com.ssverma.shared.ai.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.ssverma.shared.ai.data.local.dao.CinephileChatDao
import com.ssverma.shared.ai.data.local.entity.CinephileMessageEntity
import com.ssverma.shared.ai.data.local.entity.CinephileThreadEntity

@Database(
    entities = [
        CinephileThreadEntity::class,
        CinephileMessageEntity::class,
    ],
    version = 1,
    exportSchema = false
)
abstract class CinephileChatDatabase : RoomDatabase() {
    abstract fun chatDao(): CinephileChatDao
}
