package com.mylittlelauncher.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.mylittlelauncher.model.LauncherThemePreset

@Immutable
data class LauncherPalette(
    val bgTop: Color,
    val bgBottom: Color,
    val accent: Color,
    val accentSoft: Color,
    val ink: Color,
    val muted: Color,
    val glassSurface: Color,
    val glassBorder: Color,
    val cardSurface: Color,
    val isDark: Boolean
)

val LocalLauncherPalette = staticCompositionLocalOf {
    paletteFor(LauncherThemePreset.SOFT_MINT)
}

fun paletteFor(preset: LauncherThemePreset): LauncherPalette = when (preset) {
    LauncherThemePreset.SOFT_MINT -> LauncherPalette(
        bgTop = Color(0xFFD8EDE2),
        bgBottom = Color(0xFFF7F6F0),
        accent = Color(0xFF4F8A6F),
        accentSoft = Color(0xFFB7D7C5),
        ink = Color(0xFF222D28),
        muted = Color(0xFF5D6D65),
        glassSurface = Color(0xE8FFFFFF),
        glassBorder = Color(0x99FFFFFF),
        cardSurface = Color(0xF2FFFFFF),
        isDark = false
    )
    LauncherThemePreset.WARM_PEACH -> LauncherPalette(
        bgTop = Color(0xFFF7E1D3),
        bgBottom = Color(0xFFFAF6F2),
        accent = Color(0xFFC87D5B),
        accentSoft = Color(0xFFEBC3AE),
        ink = Color(0xFF332620),
        muted = Color(0xFF756056),
        glassSurface = Color(0xEAFFFDFB),
        glassBorder = Color(0x99FFFFFF),
        cardSurface = Color(0xF4FFFFFF),
        isDark = false
    )
    LauncherThemePreset.LAVENDER_DREAM -> LauncherPalette(
        bgTop = Color(0xFFE5DFF5),
        bgBottom = Color(0xFFF8F7FC),
        accent = Color(0xFF7264A8),
        accentSoft = Color(0xFFC8BEE6),
        ink = Color(0xFF272336),
        muted = Color(0xFF655E7A),
        glassSurface = Color(0xEAFFFFFF),
        glassBorder = Color(0x99FFFFFF),
        cardSurface = Color(0xF4FFFFFF),
        isDark = false
    )
    LauncherThemePreset.MIDNIGHT_SAGE -> LauncherPalette(
        bgTop = Color(0xFF101916),
        bgBottom = Color(0xFF182420),
        accent = Color(0xFF8CC5A8),
        accentSoft = Color(0xFF2B4239),
        ink = Color(0xFFF1F6F3),
        muted = Color(0xFFA4B8AE),
        glassSurface = Color(0xCC192622),
        glassBorder = Color(0x448CC5A8),
        cardSurface = Color(0xE61E2D28),
        isDark = true
    )
}

@Composable
fun MyLittleLauncherTheme(
    preset: LauncherThemePreset,
    content: @Composable () -> Unit
) {
    val palette = paletteFor(preset)
    val scheme = if (palette.isDark) {
        darkColorScheme(
            primary = palette.accent,
            onPrimary = Color(0xFF101916),
            secondary = palette.accentSoft,
            background = palette.bgTop,
            surface = palette.cardSurface,
            onSurface = palette.ink
        )
    } else {
        lightColorScheme(
            primary = palette.accent,
            onPrimary = Color.White,
            secondary = palette.accentSoft,
            background = palette.bgBottom,
            surface = palette.cardSurface,
            onSurface = palette.ink
        )
    }

    CompositionLocalProvider(LocalLauncherPalette provides palette) {
        MaterialTheme(
            colorScheme = scheme,
            content = content
        )
    }
}
