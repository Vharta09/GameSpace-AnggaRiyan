package com.example.gamespace

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.graphics.drawable.GradientDrawable
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import kotlin.math.abs

/**
 * Overlay Game Assistant: handle di tepi kiri, panel samping, dan HUD statistik.
 * Dibuat dengan View biasa agar ringan dan tidak butuh lifecycle Compose di overlay.
 */
class GameOverlay(private val ctx: Context, private val controller: GameController) {

    private val wm = ctx.getSystemService(WindowManager::class.java)
    private val prefs = Prefs(ctx)
    private val handler = Handler(Looper.getMainLooper())

    private var handleView: View? = null
    private var hudView: TextView? = null
    private var panelView: View? = null
    private var statsText: TextView? = null
    private var hintText: TextView? = null
    private val modePills = mutableMapOf<PerfMode, TextView>()

    private var pkg = ""
    private var settings = GameSettings()

    private val red = 0xFFFF2F45.toInt()
    private val cardColor = 0xF2141418.toInt()
    private val chipColor = 0xFF26262C.toInt()
    private val muted = 0xFFB0B0B8.toInt()

    private fun dp(v: Int) = (v * ctx.resources.displayMetrics.density).toInt()

    // ---------- siklus hidup ----------

    fun show(pkg: String, s: GameSettings) {
        hide()
        this.pkg = pkg
        this.settings = s
        if (s.showAssistant) addHandle()
        if (s.showStats) addHud()
        handler.post(refresh)
    }

    fun hide() {
        handler.removeCallbacks(refresh)
        hidePanel()
        handleView?.let { remove(it) }
        hudView?.let { remove(it) }
        handleView = null
        hudView = null
    }

    private fun remove(v: View) {
        runCatching { wm.removeViewImmediate(v) }
    }

    private val refresh = object : Runnable {
        override fun run() {
            val st = PerfMonitor.read(ctx)
            hudView?.text = "%.1f°C  •  %d%%  •  %.0f Hz  •  RAM %d%%"
                .format(st.tempC, st.battery, st.refreshHz, st.ramUsedPct)
            statsText?.text = "Suhu %.1f°C (%s)\nBaterai %d%%%s\nRAM terpakai %d%% • bebas %d MB\nLayar %.0f Hz"
                .format(
                    st.tempC, PerfMonitor.thermalLabel(st.thermalStatus),
                    st.battery, if (st.charging) " (mengisi)" else "",
                    st.ramUsedPct, st.ramFreeMb, st.refreshHz
                )
            handler.postDelayed(this, 2000)
        }
    }

    // ---------- handle & HUD ----------

    private fun params(w: Int, h: Int, gravity: Int, flags: Int) =
        WindowManager.LayoutParams(
            w, h, WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY, flags, PixelFormat.TRANSLUCENT
        ).apply { this.gravity = gravity }

    private fun addHandle() {
        val r = dp(8).toFloat()
        val v = View(ctx).apply {
            background = GradientDrawable().apply {
                setColor(0xCCFF2F45.toInt())
                cornerRadii = floatArrayOf(0f, 0f, r, r, r, r, 0f, 0f)
            }
        }
        val lp = params(
            dp(14), dp(96), Gravity.TOP or Gravity.START,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
        ).apply { x = 0; y = ctx.resources.displayMetrics.heightPixels / 3 }

        v.setOnTouchListener(object : View.OnTouchListener {
            var startY = 0f
            var startLpY = 0
            var moved = false
            override fun onTouch(view: View, e: MotionEvent): Boolean {
                when (e.action) {
                    MotionEvent.ACTION_DOWN -> { startY = e.rawY; startLpY = lp.y; moved = false }
                    MotionEvent.ACTION_MOVE -> {
                        val dy = e.rawY - startY
                        if (abs(dy) > dp(8)) moved = true
                        if (moved) {
                            lp.y = (startLpY + dy).toInt()
                            runCatching { wm.updateViewLayout(view, lp) }
                        }
                    }
                    MotionEvent.ACTION_UP -> if (!moved) togglePanel()
                }
                return true
            }
        })
        wm.addView(v, lp)
        handleView = v
    }

    private fun addHud() {
        val tv = TextView(ctx).apply {
            setTextColor(Color.WHITE)
            textSize = 11f
            setPadding(dp(10), dp(4), dp(10), dp(4))
            background = GradientDrawable().apply {
                setColor(0x99000000.toInt())
                cornerRadius = dp(10).toFloat()
            }
        }
        val lp = params(
            WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT,
            Gravity.TOP or Gravity.START,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
        ).apply { x = dp(12); y = dp(8) }
        wm.addView(tv, lp)
        hudView = tv
    }

    // ---------- panel Game Assistant ----------

    private fun togglePanel() = if (panelView == null) showPanel() else hidePanel()

    private fun showPanel() {
        val v = buildPanel()
        val lp = params(
            dp(300), WindowManager.LayoutParams.WRAP_CONTENT, Gravity.START or Gravity.CENTER_VERTICAL,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH
        ).apply { x = dp(10) }
        v.setOnTouchListener { _, e ->
            if (e.action == MotionEvent.ACTION_OUTSIDE) { hidePanel(); true } else false
        }
        wm.addView(v, lp)
        panelView = v
        handler.removeCallbacks(refresh)
        handler.post(refresh)
    }

    private fun hidePanel() {
        panelView?.let { remove(it) }
        panelView = null
        statsText = null
        hintText = null
        modePills.clear()
    }

    private fun buildPanel(): View {
        val root = LinearLayout(ctx).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(14), dp(16), dp(14))
            background = GradientDrawable().apply {
                setColor(cardColor)
                cornerRadius = dp(22).toFloat()
                setStroke(dp(1), 0x66FF2F45)
            }
        }

        root.addView(label("GAME ASSISTANT", 12f, red, bold = true).apply { letterSpacing = 0.15f })
        statsText = label("", 13f, Color.WHITE).apply { setPadding(0, dp(8), 0, dp(12)) }
        root.addView(statsText)

        // Mode performa
        root.addView(label("MODE PERFORMA", 10f, muted, bold = true).apply { letterSpacing = 0.12f })
        val modeRow = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(6), 0, dp(4))
        }
        PerfMode.entries.forEach { m ->
            val pill = label(m.label, 13f, Color.WHITE, bold = true).apply {
                gravity = Gravity.CENTER
                setPadding(0, dp(10), 0, dp(10))
                setOnClickListener { selectMode(m) }
            }
            modePills[m] = pill
            modeRow.addView(pill, LinearLayout.LayoutParams(0, -2, 1f).apply {
                marginEnd = dp(6)
            })
        }
        root.addView(modeRow)
        hintText = label("", 10f, muted).apply { setPadding(0, 0, 0, dp(10)) }
        root.addView(hintText)
        paintModes()

        // Toggle
        root.addView(toggle("Jangan ganggu", settings.dnd) { on ->
            settings = settings.copy(dnd = on); prefs.save(pkg, settings); controller.setDnd(on)
        })
        root.addView(toggle("Kunci kecerahan", settings.lockBrightness) { on ->
            settings = settings.copy(lockBrightness = on); prefs.save(pkg, settings)
            controller.setBrightnessLock(on)
        })

        // Aksi
        val row1 = LinearLayout(ctx).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(0, dp(12), 0, 0)
        }
        row1.addView(action("Screenshot") { hidePanel(); handler.postDelayed({ screenshot() }, 350) }, weight())
        row1.addView(action("Rekam") { hidePanel(); handler.postDelayed({ recordHint() }, 250) }, weight())
        row1.addView(action("Bersihkan RAM") {
            controller.cleanMemory(pkg)
            toast("RAM dibersihkan")
            handler.postDelayed(refresh, 800)
        }, weight())
        root.addView(row1)

        root.addView(action("Buka Game Space") {
            hidePanel()
            ctx.startActivity(
                Intent(ctx, MainActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })

        return root
    }

    private fun selectMode(m: PerfMode) {
        settings = settings.copy(mode = m)
        prefs.save(pkg, settings)
        val applied = controller.tryApplyGameMode(pkg, m)
        hintText?.text = if (applied) "Mode diterapkan ke sistem."
        else "Mode tersimpan. Untuk menerapkan ke sistem: ${controller.adbCommand(pkg, m)}"
        paintModes()
    }

    private fun paintModes() {
        modePills.forEach { (m, tv) ->
            tv.background = GradientDrawable().apply {
                setColor(if (m == settings.mode) red else chipColor)
                cornerRadius = dp(12).toFloat()
            }
        }
    }

    private fun screenshot() {
        val s = GameAccessibilityService.instance
        if (s == null) toast("Aktifkan layanan aksesibilitas Game Space dulu") else s.screenshot()
    }

    private fun recordHint() {
        val s = GameAccessibilityService.instance
        if (s == null) toast("Aktifkan layanan aksesibilitas Game Space dulu")
        else { s.quickSettings(); toast("Ketuk tile Perekam Layar") }
    }

    // ---------- helper tampilan ----------

    private fun weight() = LinearLayout.LayoutParams(0, -2, 1f).apply { marginEnd = dp(6) }

    private fun label(text: String, size: Float, color: Int, bold: Boolean = false) =
        TextView(ctx).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) setTypeface(typeface, android.graphics.Typeface.BOLD)
        }

    private fun toggle(text: String, checked: Boolean, onChange: (Boolean) -> Unit) =
        Switch(ctx).apply {
            this.text = text
            isChecked = checked
            textSize = 14f
            setTextColor(Color.WHITE)
            setPadding(0, dp(4), 0, dp(4))
            setOnCheckedChangeListener { _, on -> onChange(on) }
        }

    private fun action(text: String, onClick: () -> Unit) =
        label(text, 12f, Color.WHITE, bold = true).apply {
            gravity = Gravity.CENTER
            setPadding(dp(4), dp(12), dp(4), dp(12))
            background = GradientDrawable().apply {
                setColor(chipColor)
                cornerRadius = dp(12).toFloat()
            }
            setOnClickListener { onClick() }
        }

    private fun toast(msg: String) = Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show()
}
