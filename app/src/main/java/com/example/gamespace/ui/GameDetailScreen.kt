package com.example.gamespace.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.activity.compose.BackHandler
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gamespace.GameController
import androidx.compose.ui.platform.LocalContext

@Composable
fun GameDetailScreen(vm: MainViewModel, pkg: String, modifier: Modifier, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val ctx = LocalContext.current
    val apps by vm.apps.collectAsStateWithLifecycle()
    val app = apps.firstOrNull { it.pkg == pkg }
    var s by remember(pkg) { mutableStateOf(vm.settings(pkg)) }
    fun update(n: com.example.gamespace.GameSettings) { s = n; vm.save(pkg, n) }
    val controller = remember { GameController(ctx) }

    Column(
        modifier.navigationBarsPadding().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text("‹ Kembali", color = GsRed, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onBack))

        Row(verticalAlignment = Alignment.CenterVertically) {
            app?.let { AppIcon(it.icon, 72) }
            Spacer(Modifier.width(14.dp))
            Column {
                Text(app?.label ?: pkg, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(pkg, fontSize = 11.sp, color = GsMuted)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column {
                SectionTitle("Mode performa")
                ModeSelector(s.mode) { update(s.copy(mode = it)) }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Mode tingkat sistem membutuhkan izin shell. Jalankan sekali lewat komputer:",
                    fontSize = 11.sp, color = GsMuted
                )
                Text(controller.adbCommand(pkg, s.mode), fontSize = 12.sp, color = GsRed, fontWeight = FontWeight.SemiBold)
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column {
                SectionTitle("Saat game berjalan")
                SettingSwitch("Jangan ganggu", "Blokir notifikasi dan panggilan kecuali alarm", s.dnd) { update(s.copy(dnd = it)) }
                SettingSwitch("Bersihkan RAM saat dibuka", "Hentikan aplikasi latar belakang", s.cleanOnLaunch) { update(s.copy(cleanOnLaunch = it)) }
                SettingSwitch("Kunci kecerahan", "Matikan kecerahan otomatis", s.lockBrightness) { update(s.copy(lockBrightness = it)) }
            }
        }

        Card(Modifier.fillMaxWidth()) {
            Column {
                SectionTitle("Game Assistant")
                SettingSwitch("Panel samping", "Geser/ketuk tepi kiri layar untuk membuka", s.showAssistant) { update(s.copy(showAssistant = it)) }
                SettingSwitch("HUD statistik", "Suhu, baterai, Hz, dan RAM di atas layar", s.showStats) { update(s.copy(showStats = it)) }
            }
        }

        RedButton("Hapus dari daftar game", Modifier.fillMaxWidth()) {
            vm.removeGame(pkg); onBack()
        }
    }
}
