package com.mxwis.aitranslate.di

import android.content.Context
import androidx.room.Room
import com.mxwis.aitranslate.data.history.AppDatabase
import com.mxwis.aitranslate.data.model.HyMtModelManager
import com.mxwis.aitranslate.data.ocr.MlKitImageTextRecognizer
import com.mxwis.aitranslate.data.settings.SettingsStore
import com.mxwis.aitranslate.data.translation.CloudTranslationEngine
import com.mxwis.aitranslate.data.translation.OfflineTranslationEngine
import com.mxwis.aitranslate.data.translation.TranslationRepository
import com.mxwis.aitranslate.data.update.AppUpdateManager
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val database = Room.databaseBuilder(
        appContext,
        AppDatabase::class.java,
        "ai_translate.db",
    ).build()

    private val settingsStore = SettingsStore(appContext)
    private val modelManager = HyMtModelManager(appContext, httpClient)
    val imageTextRecognizer = MlKitImageTextRecognizer(appContext)

    val repository = TranslationRepository(
        settingsStore = settingsStore,
        historyDao = database.historyDao(),
        modelManager = modelManager,
        cloudEngine = CloudTranslationEngine(httpClient),
        offlineEngine = OfflineTranslationEngine(modelManager),
        appUpdateManager = AppUpdateManager(appContext, httpClient),
    )
}
