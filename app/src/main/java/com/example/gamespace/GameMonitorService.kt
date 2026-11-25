package com.example.gamespace

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.widget.Toast
import androidx.core.content.ContextCompat

/**
 * Memantau aplikasi yang sedang di depan. Saat sebuah game terbuka, terapkan pengaturan
 * game (DND, bersihkan RAM, dll.) dan tampilkan overlay Game Assistant.
 */
class GameMonitorService : Service() {

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var prefs: Prefs
    private lateinit var controller: GameController
    private lateinit var overlay: GameOverlay
    private lateinit var usm: UsageStatsManager

    private var lastQuery = 0L
    private var fgPkg: String? = null
    private var fgClass: String? = null
    private var activeGame: String? = null
    private var gameSet: Set<String> = emptySet()
    private var gameSetAt = 0L
    private var awaySince = 0L

    private val poll = object : Runnable {
        override fun run() {
            runCatching { tick() }
            handler.postDelayed(this, 1500)
        }
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        prefs = Prefs(this)
        controller = GameController(this)
        overlay = GameOverlay(this, controller)
        usm = getSystemService(UsageStatsManager::class.java)
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Game Space", NotificationManager.IMPORTANCE_LOW)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            prefs.serviceEnabled = false
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIF_ID, buildNotification(), ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
        prefs.serviceEnabled = true
        handler.removeCallbacks(poll)
        handler.post(poll)
        return START_STICKY
    }

    override fun onDestroy() {
        handler.removeCallbacks(poll)
        if (activeGame != null) leaveGame()
        super.onDestroy()
    }

    private fun tick() {
        val now = System.currentTimeMillis()
        if (now - gameSetAt > 60_000L) {
            gameSet = AppRepository.gamePackages(this, prefs)
            gameSetAt = now
        }
        if (lastQuery == 0L) lastQuery = now - 3_600_000L

        val events = usm.queryEvents(lastQuery, now)
        lastQuery = now - 2_000L
        val e = UsageEvents.Event()
        var cur = fgPkg
        var curCls = fgClass
        while (events.hasNextEvent()) {
            events.getNextEvent(e)
            when (e.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED ->
                    if (e.packageName != "com.android.systemui") {
                        cur = e.packageName; curCls = e.className
                    }
                UsageEvents.Event.ACTIVITY_PAUSED ->
                    if (e.packageName == cur && e.className == curCls) {
                        cur = null; curCls = null
                    }
            }
        }
        fgPkg = cur
        fgClass = curCls

        val game = cur?.takeIf { it in gameSet }
        if (game != null) {
            awaySince = 0L
            if (game != activeGame) {
                if (activeGame != null) leaveGame()
                enterGame(game)
            }
        } else if (activeGame != null) {
            if (awaySince == 0L) awaySince = now
            if (now - awaySince > 6_000L) leaveGame()
        }
    }

    private fun enterGame(pkg: String) {
        val s = prefs.settings(pkg)
        controller.enter(pkg, s)
        if (Perms.overlay(this)) overlay.show(pkg, s)
        activeGame = pkg
        Toast.makeText(this, "Game Space aktif", Toast.LENGTH_SHORT).show()
    }

    private fun leaveGame() {
        controller.exit()
        overlay.hide()
        activeGame = null
        awaySince = 0L
    }

    private fun buildNotification(): Notification {
        val open = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE
        )
        val stop = PendingIntent.getService(
            this, 1,
            Intent(this, GameMonitorService::class.java).setAction(ACTION_STOP),
            PendingIntent.FLAG_IMMUTABLE
        )
        return Notification.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_stat_gs)
            .setContentTitle("Game Space aktif")
            .setContentText("Menunggu game dibuka")
            .setContentIntent(open)
            .addAction(Notification.Action.Builder(null, "Matikan", stop).build())
            .setOngoing(true)
            .build()
    }

    companion object {
        private const val CHANNEL_ID = "gamespace_monitor"
        private const val NOTIF_ID = 1001
        const val ACTION_STOP = "com.example.gamespace.STOP"

        fun start(ctx: Context) {
            ContextCompat.startForegroundService(ctx, Intent(ctx, GameMonitorService::class.java))
        }

        fun stop(ctx: Context) {
            Prefs(ctx).serviceEnabled = false
            ctx.stopService(Intent(ctx, GameMonitorService::class.java))
        }
    }
}
