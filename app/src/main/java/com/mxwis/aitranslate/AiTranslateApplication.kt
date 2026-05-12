package com.mxwis.aitranslate

import android.app.Application
import com.mxwis.aitranslate.di.AppContainer

class AiTranslateApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
