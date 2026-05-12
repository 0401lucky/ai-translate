package com.mxwis.aitranslate.data.history

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [TranslationHistoryEntity::class],
    version = 1,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun historyDao(): TranslationHistoryDao
}
