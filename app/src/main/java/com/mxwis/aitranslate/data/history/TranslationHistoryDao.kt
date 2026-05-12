package com.mxwis.aitranslate.data.history

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TranslationHistoryDao {
    @Query("SELECT * FROM translation_history ORDER BY created_at DESC")
    fun observeAll(): Flow<List<TranslationHistoryEntity>>

    @Insert
    suspend fun insert(entity: TranslationHistoryEntity)

    @Delete
    suspend fun delete(entity: TranslationHistoryEntity)

    @Query("DELETE FROM translation_history")
    suspend fun clear()
}
