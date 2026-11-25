package com.example.gamespace

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * Hanya dipakai untuk aksi global (screenshot, Quick Settings) dari panel Game Assistant.
 * Layanan ini tidak membaca isi layar.
 */
class GameAccessibilityService : AccessibilityService() {

    override fun onServiceConnected() {
        super.onServiceConnected()
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) = Unit

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        if (instance === this) instance = null
        super.onDestroy()
    }

    fun screenshot() = performGlobalAction(GLOBAL_ACTION_TAKE_SCREENSHOT)

    fun quickSettings() = performGlobalAction(GLOBAL_ACTION_QUICK_SETTINGS)

    companion object {
        var instance: GameAccessibilityService? = null
            private set
    }
}
