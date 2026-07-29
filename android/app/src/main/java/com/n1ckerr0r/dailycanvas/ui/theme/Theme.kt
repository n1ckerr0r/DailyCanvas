package com.n1ckerr0r.dailycanvas.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = DeepNavy,
    secondary = AccentRed,
    background = WarmPaper,
    surface = WarmPaper,
    onPrimary = androidx.compose.ui.graphics.Color.White,
    onBackground = Ink,
    onSurface = Ink,
)

@Composable
fun DailyCanvasTheme(
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content,
    )
}
