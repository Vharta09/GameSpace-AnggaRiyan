package com.example.gamespace

import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import java.util.Calendar

object AppRepository {

    private fun launcherApps(ctx: Context): List<ApplicationInfo> {
        val pm = ctx.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        return pm.queryIntentActivities(intent, PackageManager.ResolveInfoFlags.of(0))
            .map { it.activityInfo.applicationInfo }
            .distinctBy { it.packageName }
            .filter { it.packageName != ctx.packageName }
    }

    /** Daftar lengkap aplikasi (dengan ikon) untuk UI. */
    fun load(ctx: Context): List<AppEntry> {
        val pm = ctx.packageManager
        return launcherApps(ctx)
            .map { ai ->
                AppEntry(
                    pkg = ai.packageName,
                    label = ai.loadLabel(pm).toString(),
                    icon = ai.loadIcon(pm),
                    isGame = ai.category == ApplicationInfo.CATEGORY_GAME
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    /** Hanya nama paket game (ringan, tanpa ikon) untuk service. */
    fun gamePackages(ctx: Context, prefs: Prefs): Set<String> {
        val auto = launcherApps(ctx)
            .filter { it.category == ApplicationInfo.CATEGORY_GAME }
            .map { it.packageName }
        return (auto + prefs.pinned).toSet() - prefs.hidden
    }

    /** Waktu bermain hari ini (ms) per paket. */
    fun playTimeToday(ctx: Context): Map<String, Long> {
        val usm = ctx.getSystemService(UsageStatsManager::class.java)
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return try {
            usm.queryAndAggregateUsageStats(cal.timeInMillis, System.currentTimeMillis())
                .mapValues { it.value.totalTimeInForeground }
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
