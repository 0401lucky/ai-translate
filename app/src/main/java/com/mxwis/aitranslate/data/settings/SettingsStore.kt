package com.mxwis.aitranslate.data.settings

import android.content.Context
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.mxwis.aitranslate.domain.TranslationMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

private val Context.settingsDataStore by preferencesDataStore(name = "settings")

class SettingsStore(private val context: Context) {
    private object Keys {
        val baseUrl = stringPreferencesKey("base_url")
        val apiKey = stringPreferencesKey("api_key")
        val modelName = stringPreferencesKey("model_name")
        val customModelNames = stringPreferencesKey("custom_model_names")
        val cloudProviders = stringPreferencesKey("cloud_providers")
        val selectedProviderId = stringPreferencesKey("selected_provider_id")
        val defaultMode = stringPreferencesKey("default_mode")
    }

    val settings: Flow<AppSettings> = context.settingsDataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferences -> readSettings(preferences) }

    suspend fun updateBaseUrl(value: String) {
        updateSelectedProvider { it.copy(baseUrl = value.trim()) }
    }

    suspend fun updateApiKey(value: String) {
        updateSelectedProvider { it.copy(apiKey = value.trim()) }
    }

    suspend fun updateModelName(value: String) {
        updateSelectedProvider { it.copy(modelName = value.trim()) }
    }

    suspend fun updateCustomModelNames(values: List<String>) {
        val normalized = decodeCustomModelNames(encodeCustomModelNames(values))
        updateSelectedProvider { it.copy(customModelNames = normalized) }
    }

    suspend fun updateProviderName(value: String) {
        updateSelectedProvider { provider ->
            provider.copy(name = value.trim().ifBlank { provider.name })
        }
    }

    suspend fun selectCloudProvider(providerId: String) {
        context.settingsDataStore.edit { preferences ->
            val current = readSettings(preferences)
            val provider = current.cloudProviders.firstOrNull { it.id == providerId } ?: return@edit
            writeSettings(preferences, current.withSelectedProvider(provider))
        }
    }

    suspend fun addCloudProvider(provider: CloudProviderSettings) {
        context.settingsDataStore.edit { preferences ->
            val current = readSettings(preferences)
            writeSettings(preferences, current.withSelectedProvider(provider))
        }
    }

    suspend fun updateDefaultMode(value: TranslationMode) {
        context.settingsDataStore.edit { preferences ->
            val current = readSettings(preferences)
            writeSettings(preferences, current.copy(defaultMode = value))
        }
    }

    private suspend fun updateSelectedProvider(
        transform: (CloudProviderSettings) -> CloudProviderSettings,
    ) {
        context.settingsDataStore.edit { preferences ->
            val current = readSettings(preferences)
            val updatedProvider = transform(current.selectedProvider)
            writeSettings(preferences, current.withSelectedProvider(updatedProvider))
        }
    }

    companion object {
        private fun readSettings(preferences: Preferences): AppSettings {
            val legacyBaseUrl = preferences[Keys.baseUrl] ?: DEFAULT_BASE_URL
            val legacyApiKey = preferences[Keys.apiKey].orEmpty()
            val legacyModelName = preferences[Keys.modelName] ?: DEFAULT_MODEL_NAME
            val legacyCustomModels = decodeCustomModelNames(preferences[Keys.customModelNames].orEmpty())
            val decodedProviders = decodeCloudProviders(preferences[Keys.cloudProviders].orEmpty())
            val providers = ensureDefaultProviders(
                providers = decodedProviders,
                legacyBaseUrl = legacyBaseUrl,
                legacyApiKey = legacyApiKey,
                legacyModelName = legacyModelName,
                legacyCustomModels = legacyCustomModels,
            )
            val requestedProviderId = preferences[Keys.selectedProviderId] ?: DEFAULT_PROVIDER_ID
            val selectedProvider = providers.firstOrNull { it.id == requestedProviderId }
                ?: providers.first()
            val defaultMode = runCatching {
                TranslationMode.valueOf(preferences[Keys.defaultMode] ?: TranslationMode.CLOUD.name)
            }.getOrDefault(TranslationMode.CLOUD)

            return AppSettings(
                baseUrl = selectedProvider.baseUrl,
                apiKey = selectedProvider.apiKey,
                modelName = selectedProvider.modelName,
                customModelNames = selectedProvider.customModelNames,
                cloudProviders = providers,
                selectedProviderId = selectedProvider.id,
                defaultMode = defaultMode,
            )
        }

        private fun writeSettings(
            preferences: MutablePreferences,
            settings: AppSettings,
        ) {
            val provider = settings.selectedProvider
            preferences[Keys.baseUrl] = provider.baseUrl
            preferences[Keys.apiKey] = provider.apiKey
            preferences[Keys.modelName] = provider.modelName
            preferences[Keys.customModelNames] = encodeCustomModelNames(provider.customModelNames)
            preferences[Keys.cloudProviders] = encodeCloudProviders(settings.cloudProviders)
            preferences[Keys.selectedProviderId] = provider.id
            preferences[Keys.defaultMode] = settings.defaultMode.name
        }

        internal fun encodeCustomModelNames(values: List<String>): String {
            return values
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }
                .joinToString("\n")
        }

        internal fun decodeCustomModelNames(value: String): List<String> {
            return value
                .lineSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .distinctBy { it.lowercase() }
                .toList()
        }

        internal fun encodeCloudProviders(values: List<CloudProviderSettings>): String {
            val array = JSONArray()
            values
                .distinctBy { it.id }
                .forEach { provider ->
                    array.put(
                        JSONObject()
                            .put("id", provider.id)
                            .put("name", provider.name)
                            .put("baseUrl", provider.baseUrl)
                            .put("apiKey", provider.apiKey)
                            .put("modelName", provider.modelName)
                            .put("customModelNames", JSONArray(provider.customModelNames)),
                    )
                }
            return array.toString()
        }

        internal fun decodeCloudProviders(value: String): List<CloudProviderSettings> {
            if (value.isBlank()) return emptyList()
            return runCatching {
                val array = JSONArray(value)
                buildList {
                    for (index in 0 until array.length()) {
                        val item = array.optJSONObject(index) ?: continue
                        val id = item.optString("id").trim()
                        val name = item.optString("name").trim()
                        if (id.isBlank() || name.isBlank()) continue

                        add(
                            CloudProviderSettings(
                                id = id,
                                name = name,
                                baseUrl = item.optString("baseUrl").trim(),
                                apiKey = item.optString("apiKey").trim(),
                                modelName = item.optString("modelName", DEFAULT_MODEL_NAME).trim()
                                    .ifBlank { DEFAULT_MODEL_NAME },
                                customModelNames = decodeJsonStringArray(item.optJSONArray("customModelNames")),
                            ),
                        )
                    }
                }.distinctBy { it.id }
            }.getOrDefault(emptyList())
        }

        private fun ensureDefaultProviders(
            providers: List<CloudProviderSettings>,
            legacyBaseUrl: String,
            legacyApiKey: String,
            legacyModelName: String,
            legacyCustomModels: List<String>,
        ): List<CloudProviderSettings> {
            val defaults = defaultCloudProviders(
                openAiBaseUrl = legacyBaseUrl,
                openAiApiKey = legacyApiKey,
                openAiModelName = legacyModelName,
                openAiCustomModelNames = legacyCustomModels,
            )
            return (providers.ifEmpty { defaults } + defaults)
                .distinctBy { it.id }
        }

        private fun decodeJsonStringArray(array: JSONArray?): List<String> {
            if (array == null) return emptyList()
            return buildList {
                for (index in 0 until array.length()) {
                    val value = array.optString(index).trim()
                    if (value.isNotBlank()) add(value)
                }
            }.distinctBy { it.lowercase() }
        }
    }
}
