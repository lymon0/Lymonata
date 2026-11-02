package com.lymon.app.ui

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

val Bg = Color(0xFF0B141A)
val Surface1 = Color(0xFF111B21)
val Surface2 = Color(0xFF202C33)
val Accent = Color(0xFF00A884)
val Gold = Color(0xFFFFC107)
val Blue = Color(0xFF1DA1F2)
val MineBubble = Color(0xFF005C4B)

private val scheme = darkColorScheme(
    primary = Accent, onPrimary = Color.White,
    background = Bg, onBackground = Color(0xFFE9EDEF),
    surface = Surface1, onSurface = Color(0xFFE9EDEF),
    surfaceVariant = Surface2, onSurfaceVariant = Color(0xFF8696A0),
)

@Composable
fun LymonTheme(content: @Composable () -> Unit) =
    MaterialTheme(colorScheme = scheme, content = content)

/** badge: "gold" (المشرف) أو "blue" (موثّق) */
@Composable
fun Badge(badge: String) {
    if (badge.isEmpty()) return
    Icon(Icons.Filled.Verified, contentDescription = "موثّق",
        tint = if (badge == "gold") Gold else Blue, modifier = Modifier.size(18.dp))
}
