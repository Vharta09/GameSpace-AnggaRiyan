package com.example.gamespace.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

val GsBg = Color(0xFF0A0A0C)
val GsCard = Color(0xFF16161A)
val GsChip = Color(0xFF26262C)
val GsRed = Color(0xFFFF2F45)
val GsRedDark = Color(0xFF7A0F1C)
val GsMuted = Color(0xFFA0A0AA)

val GsRedBrush = Brush.linearGradient(listOf(GsRed, GsRedDark))

private val scheme = darkColorScheme(
    primary = GsRed,
    onPrimary = Color.White,
    background = GsBg,
    onBackground = Color.White,
    surface = GsCard,
    onSurface = Color.White,
    surfaceVariant = GsChip,
    onSurfaceVariant = GsMuted
)

@Composable
fun GameSpaceTheme(content: @Composable () -> Unit) {
    // Selalu gelap, sesuai gaya Game Space.
    MaterialTheme(colorScheme = scheme, content = content)
}
