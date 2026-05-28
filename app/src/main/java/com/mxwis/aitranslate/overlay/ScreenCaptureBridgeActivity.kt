package com.mxwis.aitranslate.overlay

import android.app.Activity
import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.view.View

class ScreenCaptureBridgeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        val projectionManager = getSystemService(MediaProjectionManager::class.java)
        startActivityForResult(projectionManager.createScreenCaptureIntent(), REQUEST_SCREEN_CAPTURE)
    }

    @Deprecated("系统投屏授权页仍通过传统 Activity result 返回")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != REQUEST_SCREEN_CAPTURE) return
        val intent = Intent(this, FloatingTranslateService::class.java).apply {
            action = if (resultCode == RESULT_OK && data != null) {
                FloatingTranslateService.ACTION_SCREEN_CAPTURE_PERMISSION_GRANTED
            } else {
                FloatingTranslateService.ACTION_SCREEN_CAPTURE_PERMISSION_DENIED
            }
            putExtra(FloatingTranslateService.EXTRA_SCREEN_CAPTURE_RESULT_CODE, resultCode)
            putExtra(FloatingTranslateService.EXTRA_SCREEN_CAPTURE_DATA, data)
        }
        startService(intent)
        finishAndRemoveTask()
        overridePendingTransition(0, 0)
    }

    companion object {
        private const val REQUEST_SCREEN_CAPTURE = 41
    }
}
