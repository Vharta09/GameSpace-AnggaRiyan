package com.example.gamespace

import android.content.Context

class Prefs(context: Context) {
    private val sp = context.applicationContext
        .getSharedPreferences("gamespace", Context.MODE_PRIVATE)

    var pinned: Set<String>
        get() = sp.getStringSet("pinned", emptySet()) ?: emptySet()
        set(v) = sp.edit().putStringSet("pinned", HashSet(v)).apply()

    /** Game hasil deteksi otomatis yang disembunyikan pengguna. */
    var hidden: Set<String>
        get() = sp.getStringSet("hidden", emptySet()) ?: emptySet()
        set(v) = sp.edit().putStringSet("hidden", HashSet(v)).apply()

    var serviceEnabled: Boolean
        get() = sp.getBoolean("service_enabled", false)
        set(v) = sp.edit().putBoolean("service_enabled", v).apply()

    // Status yang disimpan agar bisa dipulihkan walau proses sempat mati.
    var prevInterruptionFilter: Int
        get() = sp.getInt("prev_filter", -1)
        set(v) = sp.edit().putInt("prev_filter", v).apply()

    var prevBrightnessMode: Int
        get() = sp.getInt("prev_brightness_mode", -1)
        set(v) = sp.edit().putInt("prev_brightness_mode", v).apply()

    fun settings(pkg: String): GameSettings {
        val d = GameSettings()
        val modeName = sp.getString("$pkg.mode", d.mode.name)
        return GameSettings(
            mode = PerfMode.entries.firstOrNull { it.name == modeName } ?: d.mode,
            dnd = sp.getBoolean("$pkg.dnd", d.dnd),
            cleanOnLaunch = sp.getBoolean("$pkg.clean", d.cleanOnLaunch),
            lockBrightness = sp.getBoolean("$pkg.brightness", d.lockBrightness),
            showAssistant = sp.getBoolean("$pkg.assistant", d.showAssistant),
            showStats = sp.getBoolean("$pkg.stats", d.showStats)
        )
    }

    fun save(pkg: String, s: GameSettings) {
        sp.edit()
            .putString("$pkg.mode", s.mode.name)
            .putBoolean("$pkg.dnd", s.dnd)
            .putBoolean("$pkg.clean", s.cleanOnLaunch)
            .putBoolean("$pkg.brightness", s.lockBrightness)
            .putBoolean("$pkg.assistant", s.showAssistant)
            .putBoolean("$pkg.stats", s.showStats)
            .apply()
    }
}
