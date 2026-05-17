package com.mxwis.aitranslate.data.dictionary

data class DictionaryEntry(
    val word: String,
    val phonetic: String,
    val definitions: List<String>,
    val translations: List<String>,
    val partOfSpeech: String,
    val tags: List<String>,
    val collins: Int,
    val oxford: Boolean,
    val bncRank: Int,
    val frequencyRank: Int,
    val inflections: List<DictionaryInflection>,
)

data class DictionaryInflection(
    val label: String,
    val value: String,
)

data class DictionaryWordSummary(
    val word: String,
    val translation: String,
)

data class DictionaryLookupResult(
    val query: String,
    val entry: DictionaryEntry?,
    val suggestions: List<DictionaryWordSummary>,
)

interface DictionaryRepositoryContract {
    suspend fun lookup(query: String): DictionaryLookupResult
    suspend fun suggest(prefix: String, limit: Int = 8): List<DictionaryWordSummary>
}
