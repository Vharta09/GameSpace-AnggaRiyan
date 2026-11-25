package com.example.gamespace

import android.graphics.drawable.Drawable

/** Mode performa. [gameModeCmd] dipakai untuk perintah `cmd game mode` (ADB/Shizuku). */
enum class PerfMode(val label: String, val gameModeCmd: String) {
    ECO("Hemat", "battery"),
    BALANCED("Seimbang", "standard"),
    PERFORMANCE("Performa", "performance")
}

data class GameSettings(
    val mode: PerfMode = PerfMode.PERFORMANCE,
    val dnd: Boolean = true,
    val cleanOnLaunch: Boolean = true,
    val lockBrightness: Boolean = false,
    val showAssistant: Boolean = true,
    val showStats: Boolean = true
)

data class AppEntry(
    val pkg: String,
    val label: String,
    val icon: Drawable,
    val isGame: Boolean
)

data class Stats(
    val tempC: Float,
    val battery: Int,
    val charging: Boolean,
    val ramUsedPct: Int,
    val ramFreeMb: Long,
    val thermalStatus: Int,
    val refreshHz: Float
)
