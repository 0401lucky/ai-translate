package com.mxwis.aitranslate.data.dictionary

import android.content.Context
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssetDictionaryRepository(
    context: Context,
    private val assetPath: String = "dictionary/ecdict_essential.tsv",
) : DictionaryRepositoryContract {
    private val appContext = context.applicationContext

    private val entries: List<DictionaryEntry> by lazy { loadEntries() }
    private val entriesByWord: Map<String, DictionaryEntry> by lazy {
        entries.associateBy { normalize(it.word) }
    }

    override suspend fun lookup(query: String): DictionaryLookupResult = withContext(Dispatchers.IO) {
        val normalized = normalize(query)
        if (normalized.isBlank()) {
            return@withContext DictionaryLookupResult(
                query = normalized,
                entry = null,
                suggestions = entries.take(8).map { it.toSummary() },
            )
        }

        val entry = entriesByWord[normalized]
        val suggestions = suggestInternal(normalized, 8)
            .filterNot { it.word.equals(entry?.word, ignoreCase = true) }
        DictionaryLookupResult(
            query = normalized,
            entry = entry,
            suggestions = suggestions,
        )
    }

    override suspend fun suggest(prefix: String, limit: Int): List<DictionaryWordSummary> = withContext(Dispatchers.IO) {
        suggestInternal(normalize(prefix), limit)
    }

    private fun loadEntries(): List<DictionaryEntry> {
        return appContext.assets.open(assetPath).bufferedReader(Charsets.UTF_8).useLines { lines ->
            lines.drop(1).mapNotNull { line ->
                val cells = line.split('\t')
                if (cells.size < 10) return@mapNotNull null
                DictionaryEntry(
                    word = cells[0].restore(),
                    phonetic = cells[1].restore(),
                    definitions = cells[2].restore().toLines(),
                    translations = cells[3].restore().toLines(),
                    partOfSpeech = cells[4].restore(),
                    collins = cells[5].toIntOrNull() ?: 0,
                    oxford = (cells[6].toIntOrNull() ?: 0) > 0,
                    tags = cells[7].restore().toTags(),
                    bncRank = cells[8].toIntOrNull() ?: 0,
                    frequencyRank = cells[9].toIntOrNull() ?: 0,
                    inflections = cells.getOrNull(10).orEmpty().restore().toInflections(),
                )
            }.toList()
        }
    }

    private fun suggestInternal(prefix: String, limit: Int): List<DictionaryWordSummary> {
        if (limit <= 0) return emptyList()
        if (prefix.isBlank()) return entries.take(limit).map { it.toSummary() }

        val prefixMatches = entries.asSequence()
            .filter { it.word.startsWith(prefix, ignoreCase = true) }
            .take(limit)
            .map { it.toSummary() }
            .toList()
        if (prefixMatches.size >= limit) return prefixMatches

        val containsMatches = entries.asSequence()
            .filter {
                !it.word.startsWith(prefix, ignoreCase = true) &&
                    it.word.contains(prefix, ignoreCase = true)
            }
            .take(limit - prefixMatches.size)
            .map { it.toSummary() }
            .toList()
        return prefixMatches + containsMatches
    }

    private fun normalize(value: String): String = value.trim().lowercase(Locale.US)

    private fun DictionaryEntry.toSummary(): DictionaryWordSummary {
        return DictionaryWordSummary(
            word = word,
            translation = translations.firstOrNull()?.compactMeaning().orEmpty(),
        )
    }

    private fun String.restore(): String = replace("\\n", "\n").trim()

    private fun String.toLines(): List<String> {
        return lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()
    }

    private fun String.compactMeaning(): String {
        return replace(Regex("^\\w+\\.\\s*"), "")
            .replace(Regex("\\s+"), " ")
            .take(42)
    }

    private fun String.toTags(): List<String> {
        val readable = mapOf(
            "zk" to "中考",
            "gk" to "高考",
            "ky" to "考研",
            "cet4" to "CET4",
            "cet6" to "CET6",
            "ielts" to "IELTS",
            "toefl" to "TOEFL",
            "gre" to "GRE",
        )
        return split(Regex("\\s+"))
            .mapNotNull { token -> readable[token.lowercase(Locale.US)] }
            .distinct()
    }

    private fun String.toInflections(): List<DictionaryInflection> {
        val labels = mapOf(
            "p" to "过去式",
            "d" to "过去分词",
            "i" to "现在分词",
            "3" to "第三人称单数",
            "s" to "复数",
            "r" to "比较级",
            "t" to "最高级",
        )
        return split('/')
            .mapNotNull { item ->
                val parts = item.split(':', limit = 2)
                if (parts.size != 2) return@mapNotNull null
                val label = labels[parts[0]] ?: return@mapNotNull null
                val value = parts[1].trim()
                if (value.isBlank()) return@mapNotNull null
                DictionaryInflection(label, value)
            }
    }
}
