package com.example.gamespace

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.display.DisplayManager
import android.os.BatteryManager
import android.os.PowerManager
import android.view.Display

object PerfMonitor {

    fun read(ctx: Context): Stats {
        val bat = ctx.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val temp = (bat?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f
        val level = bat?.getIntExtra(BatteryManager.EXTRA_LEVEL, 0) ?: 0
        val scale = bat?.getIntExtra(BatteryManager.EXTRA_SCALE, 100) ?: 100
        val status = bat?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val charging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        val am = ctx.getSystemService(ActivityManager::class.java)
        val mi = ActivityManager.MemoryInfo().also { am.getMemoryInfo(it) }
        val usedPct = if (mi.totalMem > 0) (100 - mi.availMem * 100 / mi.totalMem).toInt() else 0

        val pm = ctx.getSystemService(PowerManager::class.java)
        val hz = ctx.getSystemService(DisplayManager::class.java)
            .getDisplay(Display.DEFAULT_DISPLAY)?.refreshRate ?: 60f

        return Stats(
            tempC = temp,
            battery = if (scale > 0) level * 100 / scale else level,
            charging = charging,
            ramUsedPct = usedPct,
            ramFreeMb = mi.availMem / (1024 * 1024),
            thermalStatus = pm.currentThermalStatus,
            refreshHz = hz
        )
    }

    fun thermalLabel(status: Int) = when (status) {
        PowerManager.THERMAL_STATUS_NONE -> "Normal"
        PowerManager.THERMAL_STATUS_LIGHT -> "Hangat"
        PowerManager.THERMAL_STATUS_MODERATE -> "Panas"
        PowerManager.THERMAL_STATUS_SEVERE -> "Sangat panas"
        else -> "Kritis"
    }
}
