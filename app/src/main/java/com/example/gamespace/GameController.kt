package com.example.gamespace

import android.app.ActivityManager
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.provider.Settings

/**
 * Menerapkan / memulihkan pengaturan saat game dibuka dan ditutup.
 * Semua fitur di sini berjalan tanpa root, hanya dengan izin yang diberikan pengguna.
 */
class GameController(private val ctx: Context) {
    private val prefs = Prefs(ctx)
    private val nm = ctx.getSystemService(NotificationManager::class.java)
    private val am = ctx.getSystemService(ActivityManager::class.java)

    fun enter(pkg: String, s: GameSettings) {
        if (s.cleanOnLaunch) cleanMemory(pkg)
        setDnd(s.dnd)
        setBrightnessLock(s.lockBrightness)
    }

    fun exit() {
        setDnd(false)
        setBrightnessLock(false)
    }

    /** Aktifkan/matikan mode jangan ganggu (hanya alarm yang lolos). */
    fun setDnd(on: Boolean) {
        if (!nm.isNotificationPolicyAccessGranted) return
        if (on) {
            if (prefs.prevInterruptionFilter == -1) {
                prefs.prevInterruptionFilter = nm.currentInterruptionFilter
            }
            nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALARMS)
        } else {
            val prev = prefs.prevInterruptionFilter
            if (prev != -1) {
                nm.setInterruptionFilter(prev)
                prefs.prevInterruptionFilter = -1
            }
        }
    }

    /** Kunci kecerahan manual agar layar tidak berubah-ubah (auto-brightness dimatikan). */
    fun setBrightnessLock(on: Boolean) {
        if (!Settings.System.canWrite(ctx)) return
        val cr = ctx.contentResolver
        if (on) {
            if (prefs.prevBrightnessMode == -1) {
                prefs.prevBrightnessMode = Settings.System.getInt(
                    cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
            }
            Settings.System.putInt(
                cr, Settings.System.SCREEN_BRIGHTNESS_MODE,
                Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
            )
        } else {
            val prev = prefs.prevBrightnessMode
            if (prev != -1) {
                Settings.System.putInt(cr, Settings.System.SCREEN_BRIGHTNESS_MODE, prev)
                prefs.prevBrightnessMode = -1
            }
        }
    }

    /** Hentikan proses latar belakang aplikasi pengguna (bukan sistem) agar RAM lega. */
    fun cleanMemory(except: String?) {
        val pm = ctx.packageManager
        val home = pm.resolveActivity(
            Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME),
            PackageManager.ResolveInfoFlags.of(0)
        )?.activityInfo?.packageName
        val keep = setOfNotNull(except, ctx.packageName, home, "com.android.systemui")
        pm.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
            .asSequence()
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 }
            .filter { it.packageName !in keep }
            .forEach { am.killBackgroundProcesses(it.packageName) }
    }

    /**
     * Mode performa tingkat sistem (`cmd game mode`) butuh izin shell.
     * Aplikasi biasa tidak bisa; kembalikan false agar UI menampilkan perintah ADB.
     */
    fun tryApplyGameMode(pkg: String, mode: PerfMode): Boolean = try {
        val p = Runtime.getRuntime().exec(arrayOf("cmd", "game", "mode", mode.gameModeCmd, pkg))
        p.waitFor() == 0
    } catch (e: Exception) {
        false
    }

    fun adbCommand(pkg: String, mode: PerfMode) =
        "adb shell cmd game mode ${mode.gameModeCmd} $pkg"
}
