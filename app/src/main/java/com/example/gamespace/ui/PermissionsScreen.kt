package com.example.gamespace.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.clickable
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gamespace.Perms

@Composable
fun PermissionsScreen(vm: MainViewModel, modifier: Modifier, launch: (Intent) -> Unit, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    val p by vm.perms.collectAsStateWithLifecycle()

    Column(
        modifier.navigationBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("‹ Kembali", color = GsRed, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onBack))
        Text("Izin", fontSize = 28.sp, fontWeight = FontWeight.Black)
        Text("Semua izin diberikan manual oleh Anda lewat Pengaturan Android.", color = GsMuted, fontSize = 13.sp)

        PermRow("Akses penggunaan", "Mendeteksi game yang sedang dibuka (wajib)", p.usage, true) { launch(Perms.usageIntent()) }
        PermRow("Tampil di atas aplikasi", "Panel Game Assistant dan HUD (wajib)", p.overlay, true) { launch(Perms.overlayIntent(ctx)) }
        PermRow("Jangan ganggu", "Memblokir notifikasi saat bermain", p.dnd, false) { launch(Perms.dndIntent()) }
        PermRow("Ubah pengaturan sistem", "Kunci kecerahan", p.write, false) { launch(Perms.writeSettingsIntent(ctx)) }
        PermRow("Aksesibilitas", "Tombol screenshot dan rekam di panel", p.accessibility, false) { launch(Perms.accessibilityIntent()) }
        PermRow("Abaikan optimasi baterai", "Agar layanan tidak dimatikan sistem (disarankan)", false, false) { launch(Perms.batteryIntent()) }

        Card(Modifier.fillMaxWidth()) {
            Text(
                "Tips Poco X7 Pro (HyperOS): buka Info aplikasi Game Space → Penghemat baterai → Tanpa batasan, " +
                    "dan aktifkan Mulai otomatis. Tanpa ini HyperOS bisa mematikan layanan di latar belakang.",
                fontSize = 12.sp, color = GsMuted
            )
        }
    }
}

@Composable
private fun PermRow(title: String, desc: String, granted: Boolean, required: Boolean, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title + if (required) " *" else "", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                Text(desc, fontSize = 12.sp, color = GsMuted)
            }
            Text(if (granted) "Aktif" else "Atur", color = if (granted) GsMuted else GsRed, fontWeight = FontWeight.Bold)
        }
    }
}
