package com.mxwis.aitranslate.data.history

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "translation_history")
data class TranslationHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "source_text")
    val sourceText: String,
    @ColumnInfo(name = "translated_text")
    val translatedText: String,
    @ColumnInfo(name = "source_language")
    val sourceLanguage: String,
    @ColumnInfo(name = "target_language")
    val targetLanguage: String,
    @ColumnInfo(name = "mode")
    val mode: String,
    @ColumnInfo(name = "created_at")
    val createdAt: Long,
)
