package com.example.gamespace.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

@Composable
fun PickAppScreen(vm: MainViewModel, modifier: Modifier, onBack: () -> Unit) {
    BackHandler(onBack = onBack)
    val apps by vm.apps.collectAsStateWithLifecycle()
    val pinned by vm.pinned.collectAsStateWithLifecycle()
    val hidden by vm.hidden.collectAsStateWithLifecycle()

    Column(modifier.navigationBarsPadding().padding(16.dp)) {
        Text("‹ Kembali", color = GsRed, fontWeight = FontWeight.Bold, modifier = Modifier.clickable(onClick = onBack))
        SectionTitle("Pilih aplikasi untuk ditambahkan")
        LazyColumn {
            items(apps, key = { it.pkg }) { a ->
                val isGame = (a.isGame || a.pkg in pinned) && a.pkg !in hidden
                Row(
                    Modifier.fillMaxWidth().clickable { if (isGame) vm.removeGame(a.pkg) else vm.addGame(a.pkg) }.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppIcon(a.icon, 44)
                    Spacer(Modifier.width(12.dp))
                    Text(a.label, Modifier.weight(1f), fontSize = 15.sp)
                    Text(if (isGame) "Ditambahkan" else "Tambah", color = if (isGame) GsMuted else GsRed, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}
