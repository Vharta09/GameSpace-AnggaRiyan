package com.example.gamespace.ui

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import com.example.gamespace.PerfMode

@Composable
fun AppIcon(drawable: Drawable, size: Int = 56) {
    val bmp = remember(drawable) {
        val px = 128
        runCatching { drawable.toBitmap(px, px, Bitmap.Config.ARGB_8888) }
            .getOrElse { Bitmap.createBitmap(px, px, Bitmap.Config.ARGB_8888).also { drawable.draw(Canvas(it)) } }
            .asImageBitmap()
    }
    Image(
        bitmap = bmp,
        contentDescription = null,
        modifier = Modifier.size(size.dp).clip(RoundedCornerShape((size * 0.26f).dp))
    )
}

@Composable
fun Card(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(22.dp))
            .background(GsCard)
            .border(1.dp, Color(0x14FFFFFF), RoundedCornerShape(22.dp))
            .padding(16.dp)
    ) { content() }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text.uppercase(),
        color = GsMuted,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.5.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
fun SettingSwitch(title: String, subtitle: String, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(
        Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
            Text(subtitle, fontSize = 12.sp, color = GsMuted)
        }
        Switch(
            checked = checked,
            onCheckedChange = onChange,
            colors = SwitchDefaults.colors(
                checkedTrackColor = GsRed,
                checkedThumbColor = Color.White,
                uncheckedTrackColor = GsChip,
                uncheckedThumbColor = GsMuted,
                uncheckedBorderColor = GsChip
            )
        )
    }
}

@Composable
fun ModeSelector(selected: PerfMode, onSelect: (PerfMode) -> Unit) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        PerfMode.entries.forEach { m ->
            val on = m == selected
            Box(
                Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .background(if (on) GsRed else GsChip)
                    .clickable { onSelect(m) }
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(m.label, fontWeight = FontWeight.Bold, fontSize = 14.sp, textAlign = TextAlign.Center)
            }
        }
    }
}

@Composable
fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GsChip)
            .padding(12.dp)
    ) {
        Text(label, fontSize = 11.sp, color = GsMuted)
        Spacer(Modifier.height(4.dp))
        Text(value, fontSize = 17.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun RedButton(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier
            .clip(RoundedCornerShape(16.dp))
            .background(GsRedBrush)
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(text, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
fun Dot(color: Color) {
    Box(Modifier.size(10.dp).clip(RoundedCornerShape(5.dp)).background(color))
    Spacer(Modifier.width(8.dp))
}
