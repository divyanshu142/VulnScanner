package com.vulnscanner.sample.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Color Palette ────────────────────────────────────────────────────────────
val BackgroundDeep   = Color(0xFF0A0A14)
val BackgroundCard   = Color(0xFF12121F)
val BackgroundSurface= Color(0xFF1A1A2E)
val AccentCyan       = Color(0xFF00E5FF)
val AccentPurple     = Color(0xFF7C4DFF)
val TextPrimary      = Color(0xFFE8E8F0)
val TextSecondary    = Color(0xFF8888AA)
val DividerColor     = Color(0xFF2A2A3A)

val ColorGreen  = Color(0xFF00C853)
val ColorYellow = Color(0xFFFFD600)
val ColorOrange = Color(0xFFFF6D00)
val ColorRed    = Color(0xFFD50000)

// ── Dark Color Scheme ────────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary         = AccentCyan,
    secondary       = AccentPurple,
    background      = BackgroundDeep,
    surface         = BackgroundCard,
    onPrimary       = BackgroundDeep,
    onSecondary     = TextPrimary,
    onBackground    = TextPrimary,
    onSurface       = TextPrimary,
    surfaceVariant  = BackgroundSurface,
    outline         = DividerColor
)

@Composable
fun VulnScannerTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography  = Typography(),
        content     = content
    )
}
