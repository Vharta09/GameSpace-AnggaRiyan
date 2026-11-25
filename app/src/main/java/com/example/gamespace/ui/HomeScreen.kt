package com.example.gamespace.ui

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.gamespace.PerfMonitor

private fun fmtTime(ms: Long): String {
    val m = ms / 60_000
    return if (m >= 60) "${m / 60}j ${m % 60}m" else "${m}m"
}

@Composable
fun HomeScreen(
    vm: MainViewModel,
    modifier: Modifier,
    onOpenGame: (String) -> Unit,
    onPick: () -> Unit,
    onPerms: () -> Unit
) {
    val ctx = LocalContext.current
    val apps by vm.apps.collectAsStateWithLifecycle()
    val pinned by vm.pinned.collectAsStateWithLifecycle()
    val hidden by vm.hidden.collectAsStateWithLifecycle()
    val playTime by vm.playTime.collectAsStateWithLifecycle()
    val stats by vm.stats.collectAsStateWithLifecycle()
    val perms by vm.perms.collectAsStateWithLifecycle()
    val serviceOn by vm.serviceOn.collectAsStateWithLifecycle()

    val games = apps.filter { (it.isGame || it.pkg in pinned) && it.pkg !in hidden }
    val totalToday = games.sumOf { playTime[it.pkg] ?: 0L }

    LazyColumn(
        modifier.navigationBarsPadding(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column {
                Text("GAME SPACE", color = GsRed, fontSize = 13.sp, fontWeight = FontWeight.Black, letterSpacing = 3.sp)
                Text("Siap bermain", fontSize = 30.sp, fontWeight = FontWeight.Black)
            }
        }

        // Kartu status & tombol utama
        item {
            Card(Modifier.fillMaxWidth()) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                if (serviceOn) "Mode game aktif" else "Mode game mati",
                                fontSize = 18.sp, fontWeight = FontWeight.Bold
                            )
                            Text(
                                "Hari ini: ${fmtTime(totalToday)} bermain",
                                fontSize = 12.sp, color = GsMuted
                            )
                        }
                        Dot(if (serviceOn) GsRed else GsMuted)
                    }
                    Spacer(Modifier.height(14.dp))
                    if (!perms.essential) {
                        RedButton("Beri izin yang dibutuhkan", Modifier.fillMaxWidth(), onPerms)
                    } else {
                        RedButton(
                            if (serviceOn) "Matikan Game Space" else "Aktifkan Game Space",
                            Modifier.fillMaxWidth()
                        ) { vm.setService(!serviceOn) }
                    }
                }
            }
        }

        // Statistik perangkat
        item {
            val s = stats
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatTile("Suhu", s?.let { "%.1f°C".format(it.tempC) } ?: "-", Modifier.weight(1f))
                StatTile("Baterai", s?.let { "${it.battery}%" } ?: "-", Modifier.weight(1f))
                StatTile("RAM", s?.let { "${it.ramUsedPct}%" } ?: "-", Modifier.weight(1f))
                StatTile("Layar", s?.let { "%.0fHz".format(it.refreshHz) } ?: "-", Modifier.weight(1f))
            }
            s?.let {
                Text(
                    "Status termal: ${PerfMonitor.thermalLabel(it.thermalStatus)}",
                    color = GsMuted, fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        // Aksi cepat
        item {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                QuickAction("Bersihkan RAM", Modifier.weight(1f)) { vm.cleanNow() }
                QuickAction("Izin", Modifier.weight(1f), onPerms)
                QuickAction("Pengaturan sistem", Modifier.weight(1f)) {
                    ctx.startActivity(
                        Intent(android.provider.Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }
            }
        }

        item { SectionTitle("Game saya (${games.size})") }

        item {
            val rows = (games.size + 1) / 2 + 1
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxWidth().height((rows * 170).dp),
                userScrollEnabled = false,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(games, key = { it.pkg }) { g ->
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(GsCard)
                            .clickable { onOpenGame(g.pkg) }
                            .padding(14.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AppIcon(g.icon, 64)
                        Spacer(Modifier.height(8.dp))
                        Text(g.label, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text(
                            fmtTime(playTime[g.pkg] ?: 0L) + " hari ini",
                            fontSize = 11.sp, color = GsMuted
                        )
                        Spacer(Modifier.height(8.dp))
                        Box(
                            Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(GsRedBrush)
                                .clickable {
                                    ctx.packageManager.getLaunchIntentForPackage(g.pkg)?.let {
                                        ctx.startActivity(it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                                    }
                                }
                                .padding(horizontal = 18.dp, vertical = 7.dp)
                        ) { Text("MAIN", fontWeight = FontWeight.Black, fontSize = 12.sp, letterSpacing = 1.sp) }
                    }
                }
                item {
                    Column(
                        Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(GsChip)
                            .clickable(onClick = onPick)
                            .padding(14.dp)
                            .fillMaxWidth()
                            .height(140.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("+", fontSize = 36.sp, color = GsRed, fontWeight = FontWeight.Black)
                        Text("Tambah game", fontSize = 13.sp, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickAction(text: String, modifier: Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GsChip)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 6.dp),
        contentAlignment = Alignment.Center
    ) { Text(text, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, textAlign = TextAlign.Center) }
}
